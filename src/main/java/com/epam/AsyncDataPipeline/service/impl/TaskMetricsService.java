package com.epam.AsyncDataPipeline.service.impl;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * Service for tracking task-related metrics using Micrometer.
 * This service maintains counters for tracking the number of tasks submitted,
 * completed, and failed. The counters are registered with a meterRegistry
 * and can be incremented as tasks are processed.
 */
@Component
public class TaskMetricsService {


    @Autowired
    private MeterRegistry registry;
    private  Counter tasksSubmittedCounter;
    private  Counter tasksCompletedCounter;
    private  Counter tasksFailedCounter;

    @PostConstruct
    public void initMetrics() {
        tasksSubmittedCounter = Counter.builder("tasks.submitted")
                .description("Total number of tasks submitted")
                .register(registry);
        tasksCompletedCounter = Counter.builder("tasks.completed")
                .description("Total number of tasks completed")
                .register(registry);
        tasksFailedCounter = Counter.builder("tasks.failed")
                .description("Total number of tasks failed")
                .register(registry);
    }

    public void incrementTasksSubmitted() {
        tasksSubmittedCounter.increment();
    }

    public void incrementTasksCompleted() {
        tasksCompletedCounter.increment();
    }

    public void incrementTasksFailed() {
        tasksFailedCounter.increment();
    }


}