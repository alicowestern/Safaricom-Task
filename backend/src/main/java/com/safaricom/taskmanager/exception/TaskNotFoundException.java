package com.safaricom.taskmanager.exception;

/**
 * Thrown when a task is not found by ID.
 */
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Task not found with id: " + id);
    }
}
