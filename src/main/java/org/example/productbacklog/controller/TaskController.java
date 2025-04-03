package org.example.productbacklog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Create a new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<Task> createTask(
            @Parameter(description = "Task title", required = true) @RequestParam String title,
            @Parameter(description = "Task description", required = true) @RequestParam String description,
            @Parameter(description = "Task status") @RequestParam Task.TaskStatus status,
            @Parameter(description = "Task due date") @RequestParam LocalDateTime dueDate,
            @Parameter(description = "Task priority") @RequestParam int priority,
            @Parameter(description = "Task estimated hours") @RequestParam int estimatedHours,
            @Parameter(description = "User story for the task") @RequestParam(required = false) UserStory userStory,
            @Parameter(description = "User assigned to the task") @RequestParam(required = false) User assignedUser) {

        Task createdTask = taskService.createTask(title, description, status, dueDate, priority, estimatedHours, userStory, assignedUser);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all tasks")
    @ApiResponse(responseCode = "200", description = "List of all tasks")
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.findAll();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @Operation(summary = "Get task by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @Parameter(description = "ID of the task to retrieve", required = true) @PathVariable Long id) {

        return taskService.findById(id)
                .map(task -> new ResponseEntity<>(task, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Update a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @Parameter(description = "ID of the task to update", required = true) @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Task.TaskStatus status,
            @RequestParam(required = false) LocalDateTime dueDate,
            @RequestParam(required = false) int priority,
            @RequestParam(required = false) int estimatedHours) {

        Task updatedTask = taskService.updateTask(id, title, description, status, dueDate, priority, estimatedHours);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @Operation(summary = "Assign task to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Task or User not found")
    })
    @PostMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<Task> assignTaskToUser(
            @Parameter(description = "ID of the task to assign", required = true) @PathVariable Long taskId,
            @Parameter(description = "ID of the user to assign the task to", required = true) @PathVariable Long userId) {

        Task assignedTask = taskService.assignTaskToUser(taskId, userId);
        return new ResponseEntity<>(assignedTask, HttpStatus.OK);
    }

    @Operation(summary = "Log hours for a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hours logged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid hours")
    })
    @PostMapping("/{taskId}/log-hours")
    public ResponseEntity<Task> logHours(
            @Parameter(description = "ID of the task", required = true) @PathVariable Long taskId,
            @Parameter(description = "Number of hours to log", required = true) @RequestParam int hours) {

        Task updatedTask = taskService.logHours(taskId, hours);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @Operation(summary = "Update task status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PostMapping("/{taskId}/update-status")
    public ResponseEntity<Task> updateStatus(
            @Parameter(description = "ID of the task", required = true) @PathVariable Long taskId,
            @Parameter(description = "New status for the task", required = true) @RequestParam Task.TaskStatus status) {

        Task updatedTask = taskService.updateStatus(taskId, status);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @Operation(summary = "Delete a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID of the task to delete", required = true) @PathVariable Long id) {

        taskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Add a comment to a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment added successfully"),
            @ApiResponse(responseCode = "404", description = "Task or User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid comment content")
    })
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<Task> addComment(
            @Parameter(description = "ID of the task", required = true) @PathVariable Long taskId,
            @Parameter(description = "ID of the user adding the comment", required = true) @RequestParam Long userId,
            @Parameter(description = "Content of the comment", required = true) @RequestParam String content) {

        Task taskWithComment = taskService.addComment(taskId, userId, content);
        return new ResponseEntity<>(taskWithComment, HttpStatus.OK);
    }
}
