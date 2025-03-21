package com.epam.AsyncDataPipeline.controller;


import com.epam.AsyncDataPipeline.dto.*;
import com.epam.AsyncDataPipeline.service.TaskManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing task-related operations.
 * Provides endpoints for submitting, retrieving, and monitoring tasks.
 */
@RestController
@RequestMapping("/taskManagement")
public class TaskManagementController {

    private static final Logger logger = LoggerFactory.getLogger(TaskManagementController.class);
    @Autowired
    private TaskManagementService taskManagementService;


    @PostMapping
    @Operation(summary = "Submit a new task", description = "Creates a new task and submits it for asynchronous processing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error while task processing")
    })
    public ResponseEntity<TaskCreationResponse> submitTask(@RequestBody @Valid TaskManagementRequest taskManagement) {
        logger.info("Received request to create a task with name: {}", taskManagement.getName());

        TaskCreationResponse taskCreationResponse = new TaskCreationResponse();
        taskManagementService.submitTask(taskManagement);

        logger.info("Task submitted for processing: {}", taskManagement.getName());

        taskCreationResponse.setName(taskManagement.getName());
        taskCreationResponse.setCreationMessage("Task Management Initiation Started");
        return ResponseEntity.status(HttpStatus.CREATED).body(taskCreationResponse);
    }


    @GetMapping
    @Operation(summary = "Get all tasks", description = "Fetches a list of all tasks available in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully")
    })
    public ResponseEntity<List<TaskManagementResponse>> getAllTasks() {

        logger.info("Received request to fetch all tasks");
        List<TaskManagementResponse> listOfTaskManagementResponse = taskManagementService.getAllTasks();
        logger.info("Returning {} tasks.", listOfTaskManagementResponse.size());

        return ResponseEntity.ok(listOfTaskManagementResponse);
    }


    @GetMapping("/status/{id}")
    @Operation(summary = "Get status response by ID", description = "Fetches the status details of a specific task by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status found successfully"),
            @ApiResponse(responseCode = "404", description = "Status not found for the id")
    })
    public ResponseEntity<TaskManagementStatusResponse> getTaskById(@PathVariable Long id) {

        logger.info("Received request to fetch status of a task with ID: {}", id);

        TaskManagementStatusResponse taskManagementStatusResponse= taskManagementService.getTaskStatusById(id);
        logger.info("Returning task status details for ID: {}", id);
        return  ResponseEntity.ok(taskManagementStatusResponse);
    }



    @GetMapping("/statistics")
    @Operation(summary = "Get task statistics", description = "Retrieves aggregated task statistics.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task statistics retrieved successfully")
    })
    public ResponseEntity<TaskStatisticsResponse> getTaskStatistics() {

        logger.info("Generating task statistics for task management service");
        return ResponseEntity.ok(taskManagementService.getTaskStatistics());
    }

}
