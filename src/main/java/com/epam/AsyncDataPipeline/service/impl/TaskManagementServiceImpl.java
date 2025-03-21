package com.epam.AsyncDataPipeline.service.impl;

import com.epam.AsyncDataPipeline.constants.TaskManagementConstants;
import com.epam.AsyncDataPipeline.dto.TaskManagementRequest;
import com.epam.AsyncDataPipeline.dto.TaskManagementResponse;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the TaskManagementService interface.
 * Handles task submission, retrieval, status updates, and statistics aggregation.
 */

@Service
public class TaskManagementServiceImpl implements TaskManagementService {

    @Value("${spring.kafka.topic.name}")  // Read topic name from application.yml
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
    public void submitTask(TaskManagementRequest taskManagementRequest) {

        logger.info("Submitting task: {}", taskManagementRequest.getName());


        TaskManagement taskManagement = entityToModelMapper.mapRequestToEntity(taskManagementRequest);

        taskManagement.setStatus(TaskStatus.PENDING);


        TaskManagement taskManagementPersist = taskManagementRepository.save(taskManagement);

        logger.info("Task persisted with ID: {}", taskManagementPersist.getId());
        kafkaTemplate.send(topicName, taskManagementPersist.getId().toString());
        logger.info("Task ID {} sent to Kafka topic", taskManagementPersist.getId());

        taskMetricsService.incrementTasksSubmitted();

    }

    /**
     * Fallback method triggered when task submission fails.
     *
     * @param taskManagementRequest The task request.
     * @param exception The exception that caused the failure.
     */
    public void taskServiceFallback(TaskManagementRequest taskManagementRequest,
                                    Exception exception) {
        logger.error("Fallback triggered for submitTask due to: {}", exception.getMessage());

        TaskManagement taskManagement = entityToModelMapper.mapRequestToEntity(taskManagementRequest);

        taskManagement.setStatus(TaskStatus.FAILED);
        taskManagementRepository.save(taskManagement);

        taskMetricsService.incrementTasksFailed();

        throw new TaskProcessingException("Failed to process task: " + exception.getMessage());

    }

    /**
     * Retrieves all tasks from the database.
     *
     * @return A list of task management responses.
     */
    @Override
    public List<TaskManagementResponse> getAllTasks() {
        logger.info("Fetching all tasks from the database.");
        List<TaskManagement> taskManagementResponseList = taskManagementRepository.findAll();

        logger.info("Retrieved {} tasks from the database.", taskManagementResponseList.size());
        return entityToModelMapper.mapEntityToDtoList(taskManagementResponseList);


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

        taskMetricsService.incrementTasksSubmitted(totalTasks);

        logger.info("Total tasks retrieved: {}", totalTasks);
        Map<TaskStatus, Long> taskCountMap = tasks.stream()
                .collect(Collectors.groupingBy(TaskManagement::getStatus, Collectors.counting()));

        long completedTasks = taskCountMap.getOrDefault(TaskStatus.COMPLETED, TaskManagementConstants.DEFAULT_COUNT);
        long failedTasks = taskCountMap.getOrDefault(TaskStatus.FAILED, TaskManagementConstants.DEFAULT_COUNT);

        logger.info("Completed tasks: {}", completedTasks);
        logger.info("Failed tasks: {}", failedTasks);

        double successRate = totalTasks == 0 ? 0 : (completedTasks * TaskManagementConstants.PERCENTAGE_MULTIPLIER) / totalTasks;
        double failureRate = totalTasks == 0 ? 0 : (failedTasks * TaskManagementConstants.PERCENTAGE_MULTIPLIER) / totalTasks;

        taskMetricsService.incrementTasksCompleted(completedTasks); // Increment completed tasks counter
        taskMetricsService.incrementTasksFailed(failedTasks); // Increment failed tasks counter

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
