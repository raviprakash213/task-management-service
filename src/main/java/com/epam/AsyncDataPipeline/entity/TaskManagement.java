package com.epam.AsyncDataPipeline.entity;


import com.epam.AsyncDataPipeline.enums.TaskStatus;
import jakarta.persistence.*;


/**
 * Entity representing a task in the task management system.
 * This entity is mapped to the 'Taskmanagement' table in the database and
 * stores task-related information such as name, payload, and status.
 */

@Entity
@Table(name = "Taskmanagement")  // Updated table name
public class TaskManagement {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String payload;
    @Enumerated(EnumType.STRING) // Store as a string in the database
    @Column(name = "status") // Optional, ensures correct column mapping
    private TaskStatus status; // e.g., "pending", "processing", "completed", "failed"


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

    public TaskManagement(String name, String payload, TaskStatus status) {
        this.name = name;
        this.payload = payload;
        this.status = status;
    }

    public TaskManagement(){}
}