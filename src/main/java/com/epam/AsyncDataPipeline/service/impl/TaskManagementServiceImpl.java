package com.epam.AsyncDataPipeline.service.impl;

import com.epam.AsyncDataPipeline.constants.TaskManagementConstants;
import com.epam.AsyncDataPipeline.dto.TaskManagementRequest;
import com.epam.AsyncDataPipeline.dto.TaskManagementResponse;
import com.epam.AsyncDataPipeline.dto.TaskCreationResponse;
import com.epam.AsyncDataPipeline.dto.TaskManagementStatusResponse;
import com.epam.AsyncDataPipeline.dto.TaskStatisticsResponse;
import com.epam.AsyncDataPipeline.entity.TaskManagement;
import com.epam.AsyncDataPipeline.enums.TaskStatus;
import com.epam.AsyncDataPipeline.exception.TaskNotFoundException;
import com.epam.AsyncDataPipeline.exception.TaskProcessingException;
import com.epam.AsyncDataPipeline.mapper.EntityToModelMapper;
import com.epam.AsyncDataPipeline.repository.TaskManagementRepository;
import com.epam.AsyncDataPipeline.service.TaskManagementService;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of the TaskManagementService interface.
 * Handles task submission, retrieval, status updates, and statistics aggregation.
 */

@Service
public class TaskManagementServiceImpl implements TaskManagementService {

    @Value("${spring.kafka.topic.name}")
    private String topicName;
    @Autowired
    private EntityToModelMapper entityToModelMapper;
    @Autowired
    private TaskManagementRepository taskManagementRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private TaskMetricsService taskMetricsService;

    private static final Logger logger = LoggerFactory.getLogger(TaskManagementServiceImpl.class);


    /**
     * Submits a new task for asynchronous processing and sends it to Kafka.
     * If there is an exception while processing the task, it retries 3 times before falling back
     * Transaction management to rollback, the saved transaction if kafka send fails
     * @param taskManagementRequest The task request containing name and payload.
     */
    @Override
    @Transactional
    @Retry(name = "taskServiceRetry", fallbackMethod = "taskServiceFallback")
    public CompletableFuture<TaskCreationResponse> submitTask(TaskManagementRequest taskManagementRequest) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Submitting task: {}", taskManagementRequest.getName());

            // Convert DTO to Entity
            TaskManagement task = entityToModelMapper.mapRequestToEntity(taskManagementRequest);
            task.setStatus(TaskStatus.PENDING);

            // Persist to DB
            TaskManagement savedTask = taskManagementRepository.save(task);
            logger.info("Task persisted with ID: {}", savedTask.getId());

            // Send to Kafka
            sendToKafka(savedTask.getId().toString());

            // Increment metrics
            taskMetricsService.incrementTasksSubmitted();

            return new TaskCreationResponse(savedTask.getName(), "Task Management Initiation Started");
        });
    }

    private void sendToKafka(String taskId) {
        kafkaTemplate.send(topicName, taskId);
        logger.info("Task ID {} sent to Kafka", taskId);

    }

    private CompletableFuture<TaskCreationResponse> taskServiceFallback(TaskManagementRequest taskManagementRequest, Exception exception) {
        logger.error("Fallback triggered for submitTask due to: {}", exception.getMessage());

        // Persist to DB with failed status
        TaskManagement taskManagement = entityToModelMapper.mapRequestToEntity(taskManagementRequest);
        taskManagement.setStatus(TaskStatus.FAILED);
        taskManagementRepository.save(taskManagement);

        //Inc failed metrics
        taskMetricsService.incrementTasksFailed();

        //throw exception in the fallback
        throw new TaskProcessingException("Failed to process task: " + exception.getMessage());
    }


    /**
     * Retrieves the status of a specific task by its ID, with caching enabled.
     *
     * @param id The task ID.
     * @return The status response of the task.
     */
    @Override
    @Cacheable(value = "taskManagement", key = "#id")
    public TaskManagementStatusResponse getTaskStatusById(Long id) {
        logger.info("Fetching task with ID: {}", id);
        TaskManagement taskManagementEntity = taskManagementRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));

        logger.info("Task with ID {} found. Returning details.", id);
        return entityToModelMapper.mapEntityToStatusDto(taskManagementEntity);
    }

    /**
     * Retrieves a paginated list of all tasks from the database.
     * This method fetches tasks based on the provided pagination and sorting criteria.
     * @param pageable the pagination and sorting information
     * @return a Page TaskManagementResponse containing the paginated task data
     * */
     @Override
    public Page<TaskManagementResponse> getAllTasks(Pageable pageable) {
        logger.info("Fetching all tasks with pagination: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<TaskManagement> taskPage = taskManagementRepository.findAll(pageable);

        logger.info("Retrieved {} tasks from the database.", taskPage.getTotalElements());

        return taskPage.map(entityToModelMapper::mapEntityToDto);
    }

    /**
     * Updates the status of a task and evicts the cache entry.
     *
     * @param id         The task ID.
     * @param taskStatus The new status of the task.
     */
    @CacheEvict(value = "taskManagement", key = "#id")
    public void updateTaskStatus(Long id, TaskStatus taskStatus) {
        logger.info("Updating status for task ID: {} to {}", id, taskStatus);

        TaskManagement taskManagement = taskManagementRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("Task with ID " + id + " not found"));

        if (taskStatus.equals(TaskStatus.COMPLETED)) {
            taskMetricsService.incrementTasksCompleted();
        }
        taskManagement.setStatus(taskStatus);
        taskManagementRepository.save(taskManagement);
        logger.info("Task ID {} status updated to {}", id, taskStatus);
    }

    /**
     * Computes and returns task statistics, including total, completed, and failed tasks.
     *
     * @return A response containing task statistics.
     */
    @Override
    public TaskStatisticsResponse getTaskStatistics() {
        logger.info("Fetching all tasks from the database");
        List<TaskManagement> tasks = taskManagementRepository.findAll();

        long totalTasks = tasks.size();

        logger.info("Total tasks retrieved: {}", totalTasks);
        Map<TaskStatus, Long> taskCountMap = tasks.stream()
                .collect(Collectors.groupingBy(TaskManagement::getStatus, Collectors.counting()));

        long completedTasks = taskCountMap.getOrDefault(TaskStatus.COMPLETED, TaskManagementConstants.DEFAULT_COUNT);
        long failedTasks = taskCountMap.getOrDefault(TaskStatus.FAILED, TaskManagementConstants.DEFAULT_COUNT);

        logger.info("Completed tasks: {}", completedTasks);
        logger.info("Failed tasks: {}", failedTasks);

        double successRate = totalTasks == 0 ? 0 : (completedTasks * TaskManagementConstants.PERCENTAGE_MULTIPLIER) / totalTasks;
        double failureRate = totalTasks == 0 ? 0 : (failedTasks * TaskManagementConstants.PERCENTAGE_MULTIPLIER) / totalTasks;


        logger.info("Success rate: is {}", successRate);
        logger.info("Failure rate: is {}", failureRate);

        return new TaskStatisticsResponse(
                totalTasks,
                completedTasks,
                failedTasks,
                successRate,
                failureRate
        );
    }


}
