package com.taskmanager.dto;

import com.taskmanager.model.Project;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank(message = "Project name is required")
        @Size(max = 100)
        private String name;

        @Size(max = 2000)
        private String description;

        private LocalDateTime deadline;

        private List<Long> memberIds;
    }

    @Getter @Setter
    public static class UpdateRequest {
        @Size(max = 100)
        private String name;

        @Size(max = 2000)
        private String description;

        private Project.Status status;

        private LocalDateTime deadline;
    }

    @Getter @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime deadline;
        private UserDto.Summary owner;
        private List<UserDto.Summary> members;
        private long totalTasks;
        private long completedTasks;
        private long inProgressTasks;
        private long todoTasks;
    }

    @Getter @Builder
    public static class Summary {
        private Long id;
        private String name;
        private String status;
        private LocalDateTime deadline;
        private long totalTasks;
        private long completedTasks;
    }
}
