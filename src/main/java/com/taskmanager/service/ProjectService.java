package com.taskmanager.service;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import com.taskmanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private AuthService authService;

    @Transactional
    public ProjectDto.Response createProject(ProjectDto.CreateRequest request) {
        User currentUser = authService.getCurrentUser();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .owner(currentUser)
                .build();

        // Add members
        Set<User> members = new HashSet<>();
        members.add(currentUser); // owner is always a member
        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                userRepository.findById(memberId).ifPresent(members::add);
            }
        }
        project.setMembers(members);
        return toResponse(projectRepository.save(project));
    }

    public List<ProjectDto.Response> getMyProjects() {
        User currentUser = authService.getCurrentUser();
        List<Project> projects;

        if (currentUser.getGlobalRole() == User.Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findAllByMemberOrOwner(currentUser);
        }

        return projects.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProjectDto.Response getProject(Long projectId) {
        Project project = getProjectWithAccessCheck(projectId);
        return toResponse(project);
    }

    @Transactional
    public ProjectDto.Response updateProject(Long projectId, ProjectDto.UpdateRequest request) {
        Project project = getProjectWithAccessCheck(projectId);
        User currentUser = authService.getCurrentUser();

        // Only owner or admin can update
        if (!project.getOwner().getId().equals(currentUser.getId())
                && currentUser.getGlobalRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied: only project owner or admin can update");
        }

        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getDeadline() != null) project.setDeadline(request.getDeadline());

        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void addMember(Long projectId, Long userId) {
        Project project = getProjectWithAccessCheck(projectId);
        User currentUser = authService.getCurrentUser();

        if (!project.getOwner().getId().equals(currentUser.getId())
                && currentUser.getGlobalRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }

        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        project.getMembers().add(newMember);
        projectRepository.save(project);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        Project project = getProjectWithAccessCheck(projectId);
        User currentUser = authService.getCurrentUser();

        if (!project.getOwner().getId().equals(currentUser.getId())
                && currentUser.getGlobalRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }

        project.getMembers().removeIf(m -> m.getId().equals(userId));
        projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = getProjectWithAccessCheck(projectId);
        User currentUser = authService.getCurrentUser();

        if (!project.getOwner().getId().equals(currentUser.getId())
                && currentUser.getGlobalRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        projectRepository.delete(project);
    }

    public Project getProjectWithAccessCheck(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        User currentUser = authService.getCurrentUser();

        boolean hasAccess = currentUser.getGlobalRole() == User.Role.ADMIN
                || project.getOwner().getId().equals(currentUser.getId())
                || project.getMembers().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));

        if (!hasAccess) {
            throw new RuntimeException("Access denied to project: " + projectId);
        }
        return project;
    }

    public ProjectDto.Response toResponse(Project project) {
        List<Task> tasks = taskRepository.findByProjectId(project.getId());
        long total = tasks.size();
        long done = tasks.stream().filter(t -> t.getStatus() == Task.Status.DONE).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
        long todo = tasks.stream().filter(t -> t.getStatus() == Task.Status.TODO).count();

        return ProjectDto.Response.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus().name())
                .createdAt(project.getCreatedAt())
                .deadline(project.getDeadline())
                .owner(toUserSummary(project.getOwner()))
                .members(project.getMembers().stream().map(this::toUserSummary).collect(Collectors.toList()))
                .totalTasks(total)
                .completedTasks(done)
                .inProgressTasks(inProgress)
                .todoTasks(todo)
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
