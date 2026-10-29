package com.safaricom.taskmanager.service;

import com.safaricom.taskmanager.dto.TaskRequest;
import com.safaricom.taskmanager.dto.TaskResponse;
import com.safaricom.taskmanager.exception.TaskNotFoundException;
import com.safaricom.taskmanager.model.Task;
import com.safaricom.taskmanager.model.TaskStatus;
import com.safaricom.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic layer. Converts between DTOs and internal model,
 * applies defaults, and delegates persistence to the repository.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    // Constructor injection
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return toResponse(task);
    }

    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        // Default status to the initial state if not provided
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        // Default status to the initial state if not provided on update
        existing.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        Task saved = taskRepository.save(existing);
        return toResponse(saved);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.deleteById(id)) {
            throw new TaskNotFoundException(id);
        }
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus());
    }
}
