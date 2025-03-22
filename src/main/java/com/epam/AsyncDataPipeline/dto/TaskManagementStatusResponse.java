package com.epam.AsyncDataPipeline.dto;

import com.epam.AsyncDataPipeline.enums.TaskStatus;
public class TaskManagementStatusResponse {


    private TaskStatus status;


    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
