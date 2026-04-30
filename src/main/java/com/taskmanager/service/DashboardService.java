package com.taskmanager.service;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import com.taskmanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired private ProjectRepository projectRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private AuthService authService;
    @Autowired private ProjectService projectService;
    @Autowired private TaskService taskService;

    public DashboardDto getDashboard() {
        User currentUser = authService.getCurrentUser();

        List<Project> myProjects;
        if (currentUser.getGlobalRole() == User.Role.ADMIN) {
            myProjects = projectRepository.findAll();
        } else {
            myProjects = projectRepository.findAllByMemberOrOwner(currentUser);
        }

        List<Long> projectIds = myProjects.stream().map(Project::getId).collect(Collectors.toList());

        List<Task> myAssignedTasks = taskRepository.findByAssignee(currentUser);
        List<Task> overdueTasks = projectIds.isEmpty()
                ? List.of()
                : taskRepository.findOverdueByProjectIds(projectIds, LocalDateTime.now());

        long totalTasks = projectIds.isEmpty() ? 0
                : taskRepository.findByProjectId(-1L).size(); // will use below

        // Count tasks across all user's projects
        List<Task> allTasks = projectIds.stream()
                .flatMap(pid -> taskRepository.findByProjectId(pid).stream())
                .collect(Collectors.toList());

        long todoCount = allTasks.stream().filter(t -> t.getStatus() == Task.Status.TODO).count();
        long inProgressCount = allTasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
        long doneCount = allTasks.stream().filter(t -> t.getStatus() == Task.Status.DONE).count();
        long activeProjects = myProjects.stream().filter(p -> p.getStatus() == Project.Status.ACTIVE).count();

        List<ProjectDto.Summary> recentProjects = myProjects.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(p -> {
                    List<Task> ptasks = taskRepository.findByProjectId(p.getId());
                    return ProjectDto.Summary.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .status(p.getStatus().name())
                            .deadline(p.getDeadline())
                            .totalTasks(ptasks.size())
                            .completedTasks(ptasks.stream().filter(t -> t.getStatus() == Task.Status.DONE).count())
                            .build();
                }).collect(Collectors.toList());

        return DashboardDto.builder()
                .totalProjects(myProjects.size())
                .activeProjects(activeProjects)
                .totalTasks(allTasks.size())
                .todoTasks(todoCount)
                .inProgressTasks(inProgressCount)
                .doneTasks(doneCount)
                .overdueTasks(overdueTasks.size())
                .recentProjects(recentProjects)
                .myTasks(myAssignedTasks.stream().limit(10).map(taskService::toResponse).collect(Collectors.toList()))
                .overduedTasks(overdueTasks.stream().limit(5).map(taskService::toResponse).collect(Collectors.toList()))
                .build();
    }
}
