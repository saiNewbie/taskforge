package com.taskmanager.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserDto {

    @Getter @Builder
    public static class Summary {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String role;
    }

    @Getter @Builder
    public static class Profile {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private LocalDateTime createdAt;
        private long totalProjects;
        private long openTasks;
    }
}
