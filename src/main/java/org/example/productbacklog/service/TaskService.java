package org.example.productbacklog.service;

import org.example.productbacklog.dto.TaskDTO;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.entity.UserStory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskService {

    TaskDTO createTask(String title, String description, Task.TaskStatus status,
                       LocalDateTime dueDate, int priority, int estimatedHours,
                       Long userStoryId, Long assignedUserId);

    Optional<TaskDTO> findById(Long id);

    List<TaskDTO> findAll();

    List<TaskDTO> findByUserStoryId(Long userStoryId);

    List<TaskDTO> findByAssignedUserId(Long userId);

    List<TaskDTO> findByStatus(Task.TaskStatus status);

    TaskDTO updateTask(Long id, String title, String description, Task.TaskStatus status,
                       LocalDateTime dueDate, int priority, int estimatedHours);

    TaskDTO assignTaskToUser(Long taskId, Long userId);

    TaskDTO logHours(Long taskId, int hours);

    TaskDTO updateStatus(Long taskId, Task.TaskStatus status);

    void deleteTask(Long id);

    TaskDTO addComment(Long taskId, Long userId, String content);
}