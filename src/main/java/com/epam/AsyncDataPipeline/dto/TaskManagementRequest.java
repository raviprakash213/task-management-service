package com.epam.AsyncDataPipeline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TaskManagementRequest {


    @NotBlank(message = "Name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Name must contain only letters (A-Z, a-z) and spaces")
    @Size(max = 50, message = "Name must be at most 50 characters")
    private String name;

    @NotBlank(message = "Payload cannot be blank")
    @Size(max = 255, message = "Payload must be at most 255 characters")
    private String payload;

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


}
