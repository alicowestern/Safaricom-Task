package com.safaricom.taskmanager.dto;

import com.safaricom.taskmanager.model.TaskStatus;

/**
 * Response DTO returned by the API. Decouples the internal model from the API contract.
 */
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;

    public TaskResponse() {
    }

    public TaskResponse(Long id, String title, String description, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
