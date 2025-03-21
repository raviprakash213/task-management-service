package com.epam.AsyncDataPipeline.kakfaConsumer;

import com.epam.AsyncDataPipeline.constants.TaskManagementConstants;
import com.epam.AsyncDataPipeline.enums.TaskStatus;
import com.epam.AsyncDataPipeline.exception.TaskProcessingException;
import com.epam.AsyncDataPipeline.service.TaskManagementService;
import com.epam.AsyncDataPipeline.service.impl.TaskMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Kafka consumer service for processing tasks asynchronously.
 * This service listens to a Kafka topic, processes task messages,
 * updates task statuses, and tracks task metrics.
 */
@Service
public class TaskManagementConsumer {


    @Value("${spring.kafka.topic.name}")
    public String topicName;

    @Value("${spring.kafka.consumer.group-id}")
    public String groupId;

    private static final Logger logger = LoggerFactory.getLogger(TaskManagementConsumer.class);
    @Autowired
    private TaskManagementService taskManagementService;

    @Autowired
    private TaskMetricsService taskMetricsService;

    /**
     * Kafka listener that processes task messages from the configured topic.
     * Implements a retry mechanism for handling transient failures.
     *
     * @param taskId the task ID received from Kafka as a string
     * @return a CompletableFuture representing the asynchronous task processing
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            include = {TaskProcessingException.class}
    )
    @KafkaListener(topics = "#{__listener.topicName}", groupId = "#{__listener.groupId}")
    public CompletableFuture<Void> processTask(Long taskId) {
        logger.info("Received task ID: {}", taskId);

        return CompletableFuture.runAsync(() -> {
                    logger.info("Updating task {} status to PROCESSING", taskId);
                    taskManagementService.updateTaskStatus(taskId, TaskStatus.PROCESSING);
                })
                .thenRunAsync(() -> handleTask(taskId), CompletableFuture.delayedExecutor(TaskManagementConstants.THREAD_DELAY_MS, TimeUnit.MILLISECONDS))
                .thenRunAsync(() -> {
                    logger.info("Updating task {} status to COMPLETED", taskId);
                    taskManagementService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
                    taskMetricsService.incrementTasksCompleted();
                })
                .exceptionally(exception -> handleProcessingError(taskId, exception));
    }



    /**
     * Handles the actual processing of the task.
     * Simulates a delay before marking the task as processed.
     *
     * @param taskId the task ID to process
     */
    private void handleTask(Long taskId) {

        logger.info("Processing task {} with a simulated delay of {} ms", taskId, TaskManagementConstants.THREAD_DELAY_MS);
        logger.info("Task {} processing completed successfully", taskId);
    }
    /**
     * Handles errors that occur during task processing.
     * Updates the task status to FAILED and increments the failure metric.
     *
     * @param taskId    the task ID that encountered an error
     * @param exception the exception that occurred
     * @return null (used for CompletableFuture exceptionally handling)
     */
    private Void handleProcessingError(Long taskId, Throwable exception) {
        logger.error("Error processing task {}: {}", taskId, exception.getMessage(), exception);
        taskManagementService.updateTaskStatus(taskId, TaskStatus.FAILED);
        taskMetricsService.incrementTasksFailed();
        throw new TaskProcessingException("Failed to process task: " + exception.getMessage());
    }
}
