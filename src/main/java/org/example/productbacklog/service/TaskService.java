package org.example.productbacklog.service;

import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.entity.UserStory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskService {

    Task createTask(String title, String description, Task.TaskStatus status,
                    LocalDateTime dueDate, int priority, int estimatedHours,
                    UserStory userStory, User assignedUser);

    Optional<Task> findById(Long id);

    List<Task> findAll();

    List<Task> findByUserStory(UserStory userStory);

    List<Task> findByAssignedUser(User user);

    List<Task> findByStatus(Task.TaskStatus status);

    Task updateTask(Long id, String title, String description, Task.TaskStatus status,
                    LocalDateTime dueDate, int priority, int estimatedHours);

    Task assignTaskToUser(Long taskId, Long userId);

    Task logHours(Long taskId, int hours);

    Task updateStatus(Long taskId, Task.TaskStatus status);

    void deleteTask(Long id);

    Task addComment(Long taskId, Long userId, String content);
}