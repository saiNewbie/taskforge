package com.taskmanager.controller;

import com.taskmanager.dto.DashboardDto;
import com.taskmanager.dto.UserDto;
import com.taskmanager.model.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.AuthService;
import com.taskmanager.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @Autowired private DashboardService dashboardService;
    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private TaskRepository taskRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    // User profile
    @GetMapping("/users/me")
    public ResponseEntity<?> getProfile() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(UserDto.Profile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getGlobalRole().name())
                .createdAt(user.getCreatedAt())
                .totalProjects(projectRepository.countByMemberOrOwner(user))
                .openTasks(taskRepository.countOpenTasksByAssignee(user))
                .build());
    }

    // List all users (admin or for assigning tasks)
    @GetMapping("/users")
    public ResponseEntity<List<UserDto.Summary>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .map(u -> UserDto.Summary.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getGlobalRole().name())
                        .build())
                .collect(Collectors.toList()));
    }

    // Admin: promote user to admin
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{id}/promote")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setGlobalRole(User.Role.ADMIN);
        userRepository.save(user);
        return ResponseEntity.ok("User promoted to ADMIN");
    }

    // Admin: list all users with full info
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public ResponseEntity<List<UserDto.Summary>> adminGetAllUsers() {
        return getAllUsers();
    }
}
