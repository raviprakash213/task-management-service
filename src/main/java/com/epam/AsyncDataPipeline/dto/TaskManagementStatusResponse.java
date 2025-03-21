package com.epam.AsyncDataPipeline.dto;

import com.epam.AsyncDataPipeline.enums.TaskStatus;
/**
 * DTO representing the status of a task in the system.
 */
public class TaskManagementStatusResponse {


    private TaskStatus status;


    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
