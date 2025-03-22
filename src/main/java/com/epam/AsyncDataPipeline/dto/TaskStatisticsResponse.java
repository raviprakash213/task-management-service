package com.epam.AsyncDataPipeline.dto;

public class TaskStatisticsResponse {

    private long totalTasks;

    public TaskStatisticsResponse(long totalTasks, long completedTasks, long failedTasks, double successRate, double failureRate) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.failedTasks = failedTasks;
        this.successRate = successRate;
        this.failureRate = failureRate;
    }

    private long completedTasks;
    private long failedTasks;
    private double successRate;  // Changed to double for easier calculations
    private double failureRate;

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public long getFailedTasks() {
        return failedTasks;
    }

    public void setFailedTasks(long failedTasks) {
        this.failedTasks = failedTasks;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }
}
