package com.taskmanager.dto;

import com.taskmanager.model.Task;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class TaskDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank(message = "Task title is required")
        @Size(max = 200)
        private String title;

        @Size(max = 2000)
        private String description;

        private Task.Priority priority = Task.Priority.MEDIUM;

        private Long assigneeId;

        private LocalDateTime dueDate;
    }

    @Getter @Setter
    public static class UpdateRequest {
        @Size(max = 200)
        private String title;

        @Size(max = 2000)
        private String description;

        private Task.Status status;

        private Task.Priority priority;

        private Long assigneeId;

        private LocalDateTime dueDate;
    }

    @Getter @Builder
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private String status;
        private String priority;
        private Long projectId;
        private String projectName;
        private UserDto.Summary assignee;
        private UserDto.Summary createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime dueDate;
        private LocalDateTime completedAt;
        private boolean overdue;
    }
}
