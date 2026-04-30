package com.taskmanager.controller;

import com.taskmanager.dto.TaskDto;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Create task in a project
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<?> create(@PathVariable Long projectId,
                                    @Valid @RequestBody TaskDto.CreateRequest request) {
        try {
            return ResponseEntity.ok(taskService.createTask(projectId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all tasks in a project
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskDto.Response>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    // Get single task
    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.getTask(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Update task
    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody TaskDto.UpdateRequest request) {
        try {
            return ResponseEntity.ok(taskService.updateTask(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Delete task
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok("Task deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get tasks assigned to current user
    @GetMapping("/tasks/my")
    public ResponseEntity<List<TaskDto.Response>> getMyTasks() {
        return ResponseEntity.ok(taskService.getMyTasks());
    }
}
