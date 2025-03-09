package org.example.productbacklog.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.productbacklog.entity.Comment;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.repository.CommentRepository;
import org.example.productbacklog.repository.TaskRepository;
import org.example.productbacklog.repository.UserRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserStoryRepository userStoryRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository,
                           UserRepository userRepository,
                           UserStoryRepository userStoryRepository,
                           CommentRepository commentRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.userStoryRepository = userStoryRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public Task createTask(String title, String description, Task.TaskStatus status,
                           LocalDateTime dueDate, int priority, int estimatedHours,
                           UserStory userStory, User assignedUser) {

        if (userStory != null && userStory.getId() != null) {
            userStoryRepository.findById(userStory.getId())
                    .orElseThrow(() -> new EntityNotFoundException("UserStory not found with id: " + userStory.getId()));
        }

        if (assignedUser != null && assignedUser.getId() != null) {
            userRepository.findById(assignedUser.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + assignedUser.getId()));
        }

        Task task = Task.builder()
                .title(title)
                .description(description)
                .status(status != null ? status : Task.TaskStatus.TO_DO)
                .dueDate(dueDate)
                .priority(priority)
                .estimatedHours(estimatedHours)
                .loggedHours(0)
                .userStory(userStory)
                .assignedUser(assignedUser)
                .build();

        return taskRepository.save(task);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public List<Task> findByUserStory(UserStory userStory) {
        return taskRepository.findByUserStory(userStory);
    }

    @Override
    public List<Task> findByAssignedUser(User user) {
        return taskRepository.findByAssignedUser(user);
    }

    @Override
    public List<Task> findByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Task updateTask(Long id, String title, String description, Task.TaskStatus status,
                           LocalDateTime dueDate, int priority, int estimatedHours) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        if (title != null) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (status != null) {
            task.setStatus(status);
        }
        if (dueDate != null) {
            task.setDueDate(dueDate);
        }
        if (priority > 0) {
            task.setPriority(priority);
        }
        if (estimatedHours > 0) {
            task.setEstimatedHours(estimatedHours);
        }

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task assignTaskToUser(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        task.setAssignedUser(user);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task logHours(Long taskId, int hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be greater than zero");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        task.setLoggedHours(task.getLoggedHours() + hours);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task updateStatus(Long taskId, Task.TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        task.setStatus(status);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public Task addComment(Long taskId, Long userId, String content) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Comment comment = Comment.builder()
                .content(content)
                .createdAt(LocalDateTime.now())
                .task(task)
                .user(user)
                .build();

        comment = commentRepository.save(comment);

        List<Comment> comments = task.getComments();
        comments.add(comment);
        task.setComments(comments);

        return taskRepository.save(task);
    }
}