package com.epam.AsyncDataPipeline.service;

import com.epam.AsyncDataPipeline.dto.TaskManagementRequest;
import com.epam.AsyncDataPipeline.dto.TaskManagementResponse;
import com.epam.AsyncDataPipeline.dto.TaskManagementStatusResponse;
import com.epam.AsyncDataPipeline.dto.TaskStatisticsResponse;
import com.epam.AsyncDataPipeline.enums.TaskStatus;

import java.util.List;

public interface TaskManagementService {
     void submitTask(TaskManagementRequest taskManagement);

     List<TaskManagementResponse> getAllTasks();

     void updateTaskStatus(Long id, TaskStatus taskStatus);

     TaskStatisticsResponse getTaskStatistics();

     TaskManagementStatusResponse getTaskStatusById(Long id);
}
