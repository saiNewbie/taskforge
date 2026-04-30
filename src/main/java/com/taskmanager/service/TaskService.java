package com.taskmanager.service;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import com.taskmanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectService projectService;
    @Autowired private AuthService authService;

    @Transactional
    public TaskDto.Response createTask(Long projectId, TaskDto.CreateRequest request) {
        Project project = projectService.getProjectWithAccessCheck(projectId);
        User currentUser = authService.getCurrentUser();

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .project(project)
                .assignee(assignee)
                .createdBy(currentUser)
                .dueDate(request.getDueDate())
                .build();

        return toResponse(taskRepository.save(task));
    }

    public List<TaskDto.Response> getTasksByProject(Long projectId) {
        projectService.getProjectWithAccessCheck(projectId);
        return taskRepository.findByProjectId(projectId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TaskDto.Response getTask(Long taskId) {
        Task task = getTaskWithAccessCheck(taskId);
        return toResponse(task);
    }

    @Transactional
    public TaskDto.Response updateTask(Long taskId, TaskDto.UpdateRequest request) {
        Task task = getTaskWithAccessCheck(taskId);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == Task.Status.DONE && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            } else if (request.getStatus() != Task.Status.DONE) {
                task.setCompletedAt(null);
            }
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        }

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = getTaskWithAccessCheck(taskId);
        User currentUser = authService.getCurrentUser();

        boolean canDelete = currentUser.getGlobalRole() == User.Role.ADMIN
                || task.getCreatedBy().getId().equals(currentUser.getId())
                || task.getProject().getOwner().getId().equals(currentUser.getId());

        if (!canDelete) throw new RuntimeException("Access denied: cannot delete this task");
        taskRepository.delete(task);
    }

    public List<TaskDto.Response> getMyTasks() {
        User currentUser = authService.getCurrentUser();
        return taskRepository.findByAssignee(currentUser)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Task getTaskWithAccessCheck(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        // Reuse project access check
        projectService.getProjectWithAccessCheck(task.getProject().getId());
        return task;
    }

    public TaskDto.Response toResponse(Task task) {
        boolean overdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDateTime.now())
                && task.getStatus() != Task.Status.DONE;

        return TaskDto.Response.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignee(task.getAssignee() != null ? toUserSummary(task.getAssignee()) : null)
                .createdBy(toUserSummary(task.getCreatedBy()))
                .createdAt(task.getCreatedAt())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .overdue(overdue)
                .build();
    }

    private UserDto.Summary toUserSummary(User user) {
        return UserDto.Summary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getGlobalRole().name())
                .build();
    }
}
