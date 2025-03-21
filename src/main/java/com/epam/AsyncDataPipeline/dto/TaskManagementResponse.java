package com.epam.AsyncDataPipeline.dto;

import com.epam.AsyncDataPipeline.enums.TaskStatus;
/**
 * DTO representing the response details of a task in the system.
 */
public class TaskManagementResponse {

    private Long id;
    private String name;
    private String payload;
    private TaskStatus status;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
