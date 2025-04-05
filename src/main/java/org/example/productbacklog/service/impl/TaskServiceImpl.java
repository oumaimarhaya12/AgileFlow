package org.example.productbacklog.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.productbacklog.converter.TaskConverter;
import org.example.productbacklog.dto.TaskDTO;
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
    private final TaskConverter taskConverter;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository,
                           UserRepository userRepository,
                           UserStoryRepository userStoryRepository,
                           CommentRepository commentRepository,
                           TaskConverter taskConverter) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.userStoryRepository = userStoryRepository;
        this.commentRepository = commentRepository;
        this.taskConverter = taskConverter;
    }

    @Override
    @Transactional
    public TaskDTO createTask(String title, String description, Task.TaskStatus status,
                              LocalDateTime dueDate, int priority, int estimatedHours,
                              Long userStoryId, Long assignedUserId) {

        UserStory userStory = null;
        User assignedUser = null;

        if (userStoryId != null) {
            userStory = userStoryRepository.findById(userStoryId)
                    .orElseThrow(() -> new EntityNotFoundException("UserStory not found with id: " + userStoryId));
        }

        if (assignedUserId != null) {
            assignedUser = userRepository.findById(assignedUserId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + assignedUserId));
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

        Task savedTask = taskRepository.save(task);
        return taskConverter.convertToDTO(savedTask);
    }

    @Override
    public Optional<TaskDTO> findById(Long id) {
        return taskRepository.findById(id)
                .map(taskConverter::convertToDTO);
    }

    @Override
    public List<TaskDTO> findAll() {
        List<Task> tasks = taskRepository.findAll();
        return taskConverter.convertToDTOList(tasks);
    }

    @Override
    public List<TaskDTO> findByUserStoryId(Long userStoryId) {
        List<Task> tasks = taskRepository.findByUserStoryId(userStoryId);
        return taskConverter.convertToDTOList(tasks);
    }

    @Override
    public List<TaskDTO> findByAssignedUserId(Long userId) {
        List<Task> tasks = taskRepository.findByAssignedUserId(userId);
        return taskConverter.convertToDTOList(tasks);
    }

    @Override
    public List<TaskDTO> findByStatus(Task.TaskStatus status) {
        List<Task> tasks = taskRepository.findByStatus(status);
        return taskConverter.convertToDTOList(tasks);
    }

    @Override
    @Transactional
    public TaskDTO updateTask(Long id, String title, String description, Task.TaskStatus status,
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

        Task updatedTask = taskRepository.save(task);
        return taskConverter.convertToDTO(updatedTask);
    }

    @Override
    @Transactional
    public TaskDTO assignTaskToUser(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        task.setAssignedUser(user);
        Task updatedTask = taskRepository.save(task);
        return taskConverter.convertToDTO(updatedTask);
    }

    @Override
    @Transactional
    public TaskDTO logHours(Long taskId, int hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be greater than zero");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        task.setLoggedHours(task.getLoggedHours() + hours);
        Task updatedTask = taskRepository.save(task);
        return taskConverter.convertToDTO(updatedTask);
    }

    @Override
    @Transactional
    public TaskDTO updateStatus(Long taskId, Task.TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        return taskConverter.convertToDTO(updatedTask);
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
    public TaskDTO addComment(Long taskId, Long userId, String content) {
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

        Task updatedTask = taskRepository.save(task);
        return taskConverter.convertToDTO(updatedTask);
    }
}