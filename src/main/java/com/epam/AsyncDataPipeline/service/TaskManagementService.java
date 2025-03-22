package com.epam.AsyncDataPipeline.service;

import com.epam.AsyncDataPipeline.dto.TaskManagementRequest;
import com.epam.AsyncDataPipeline.dto.TaskManagementResponse;
import com.epam.AsyncDataPipeline.dto.TaskManagementStatusResponse;
import com.epam.AsyncDataPipeline.dto.TaskStatisticsResponse;
import com.epam.AsyncDataPipeline.dto.TaskCreationResponse;
import com.epam.AsyncDataPipeline.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TaskManagementService {
     CompletableFuture<TaskCreationResponse> submitTask(TaskManagementRequest taskManagement);


     void updateTaskStatus(Long id, TaskStatus taskStatus);

     TaskStatisticsResponse getTaskStatistics();

     TaskManagementStatusResponse getTaskStatusById(Long id);

     Page<TaskManagementResponse> getAllTasks(Pageable pageable);
}
