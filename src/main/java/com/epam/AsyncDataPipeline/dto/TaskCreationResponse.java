package com.epam.AsyncDataPipeline.dto;

/**
 * Data Transfer Object (DTO) for representing the response of a task creation request.
 * This class contains details about the created task, including its name and a
 * confirmation message.
 */
public class TaskCreationResponse {

    private String name;

    private String creationMessage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreationMessage() {
        return creationMessage;
    }

    public void setCreationMessage(String creationMessage) {
        this.creationMessage = creationMessage;
    }

}
