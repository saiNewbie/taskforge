package com.taskmanager.dto;

import lombok.*;
import java.util.List;

@Getter @Builder
public class DashboardDto {
    private long totalProjects;
    private long activeProjects;
    private long totalTasks;
    private long todoTasks;
    private long inProgressTasks;
    private long doneTasks;
    private long overdueTasks;
    private List<ProjectDto.Summary> recentProjects;
    private List<TaskDto.Response> myTasks;
    private List<TaskDto.Response> overduedTasks;
}
