package com.epam.AsyncDataPipeline.dto;

public class TaskCreationResponse {

    private String name;

    private String creationMessage;

    public TaskCreationResponse(String name, String creationMessage) {
        this.name=name;
        this.creationMessage=creationMessage;
    }

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
