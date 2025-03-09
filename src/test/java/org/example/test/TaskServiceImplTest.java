package org.example.test;

import org.example.productbacklog.entity.Comment;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.repository.CommentRepository;
import org.example.productbacklog.repository.TaskRepository;
import org.example.productbacklog.repository.UserRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User testUser;
    private UserStory testUserStory;
    private Task testTask;
    private LocalDateTime testDueDate;

    @BeforeEach
    void setUp() {
        testDueDate = LocalDateTime.now().plusDays(7);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        // Create UserStory using constructor instead of builder
        testUserStory = new UserStory();
        testUserStory.setId(1L);
        testUserStory.setTitle("Test User Story");

        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.TO_DO)
                .dueDate(testDueDate)
                .priority(1)
                .estimatedHours(8)
                .loggedHours(0)
                .userStory(testUserStory)
                .assignedUser(testUser)
                .comments(new ArrayList<>())
                .build();
    }

    @Test
    void createTask_ValidInputs_ReturnsCreatedTask() {
        // Arrange
        when(userStoryRepository.findById(testUserStory.getId())).thenReturn(Optional.of(testUserStory));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        Task createdTask = taskService.createTask(
                "Test Task",
                "Test Description",
                Task.TaskStatus.TO_DO,
                testDueDate,
                1,
                8,
                testUserStory,
                testUser
        );

        // Assert
        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        assertEquals("Test Description", createdTask.getDescription());
        assertEquals(Task.TaskStatus.TO_DO, createdTask.getStatus());
        assertEquals(testDueDate, createdTask.getDueDate());
        assertEquals(1, createdTask.getPriority());
        assertEquals(8, createdTask.getEstimatedHours());
        assertEquals(0, createdTask.getLoggedHours());
        assertEquals(testUserStory, createdTask.getUserStory());
        assertEquals(testUser, createdTask.getAssignedUser());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WithInvalidUserStory_ThrowsEntityNotFoundException() {
        // Arrange
        when(userStoryRepository.findById(testUserStory.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.createTask(
                    "Test Task",
                    "Test Description",
                    Task.TaskStatus.TO_DO,
                    testDueDate,
                    1,
                    8,
                    testUserStory,
                    null
            );
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_WithInvalidUser_ThrowsEntityNotFoundException() {
        // Arrange
        when(userStoryRepository.findById(testUserStory.getId())).thenReturn(Optional.of(testUserStory));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.createTask(
                    "Test Task",
                    "Test Description",
                    Task.TaskStatus.TO_DO,
                    testDueDate,
                    1,
                    8,
                    testUserStory,
                    testUser
            );
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void findById_ExistingId_ReturnsTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act
        Optional<Task> result = taskService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTask, result.get());
    }

    @Test
    void findById_NonExistingId_ReturnsEmptyOptional() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Task> result = taskService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ReturnsList() {
        // Arrange
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findAll()).thenReturn(tasks);

        // Act
        List<Task> result = taskService.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTask, result.get(0));
    }

    @Test
    void findByUserStory_ReturnsList() {
        // Arrange
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findByUserStory(testUserStory)).thenReturn(tasks);

        // Act
        List<Task> result = taskService.findByUserStory(testUserStory);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTask, result.get(0));
    }

    @Test
    void findByAssignedUser_ReturnsList() {
        // Arrange
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findByAssignedUser(testUser)).thenReturn(tasks);

        // Act
        List<Task> result = taskService.findByAssignedUser(testUser);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTask, result.get(0));
    }

    @Test
    void findByStatus_ReturnsList() {
        // Arrange
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findByStatus(Task.TaskStatus.TO_DO)).thenReturn(tasks);

        // Act
        List<Task> result = taskService.findByStatus(Task.TaskStatus.TO_DO);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTask, result.get(0));
    }

    @Test
    void updateTask_ExistingTask_ReturnsUpdatedTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Updated Task")
                .description("Updated Description")
                .status(Task.TaskStatus.IN_PROGRESS)
                .dueDate(testDueDate.plusDays(1))
                .priority(2)
                .estimatedHours(16)
                .loggedHours(0)
                .userStory(testUserStory)
                .assignedUser(testUser)
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // Act
        Task result = taskService.updateTask(
                1L,
                "Updated Task",
                "Updated Description",
                Task.TaskStatus.IN_PROGRESS,
                testDueDate.plusDays(1),
                2,
                16
        );

        // Assert
        assertNotNull(result);
        assertEquals("Updated Task", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(Task.TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(testDueDate.plusDays(1), result.getDueDate());
        assertEquals(2, result.getPriority());
        assertEquals(16, result.getEstimatedHours());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_NonExistingTask_ThrowsEntityNotFoundException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.updateTask(
                    999L,
                    "Updated Task",
                    "Updated Description",
                    Task.TaskStatus.IN_PROGRESS,
                    testDueDate.plusDays(1),
                    2,
                    16
            );
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void assignTaskToUser_ExistingTaskAndUser_ReturnsUpdatedTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        User newUser = User.builder()
                .id(2L)
                .username("newuser")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.TO_DO)
                .dueDate(testDueDate)
                .priority(1)
                .estimatedHours(8)
                .loggedHours(0)
                .userStory(testUserStory)
                .assignedUser(newUser)
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // Act
        Task result = taskService.assignTaskToUser(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(newUser, result.getAssignedUser());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void assignTaskToUser_NonExistingTask_ThrowsEntityNotFoundException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.assignTaskToUser(999L, 1L);
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void assignTaskToUser_NonExistingUser_ThrowsEntityNotFoundException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.assignTaskToUser(1L, 999L);
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void logHours_ExistingTask_ReturnsUpdatedTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.TO_DO)
                .dueDate(testDueDate)
                .priority(1)
                .estimatedHours(8)
                .loggedHours(4) // Updated logged hours
                .userStory(testUserStory)
                .assignedUser(testUser)
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // Act
        Task result = taskService.logHours(1L, 4);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getLoggedHours());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void logHours_NonExistingTask_ThrowsEntityNotFoundException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.logHours(999L, 4);
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void logHours_NegativeHours_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.logHours(1L, -4);
        });

        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateStatus_ExistingTask_ReturnsUpdatedTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.IN_PROGRESS) // Updated status
                .dueDate(testDueDate)
                .priority(1)
                .estimatedHours(8)
                .loggedHours(0)
                .userStory(testUserStory)
                .assignedUser(testUser)
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // Act
        Task result = taskService.updateStatus(1L, Task.TaskStatus.IN_PROGRESS);

        // Assert
        assertNotNull(result);
        assertEquals(Task.TaskStatus.IN_PROGRESS, result.getStatus());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateStatus_NonExistingTask_ThrowsEntityNotFoundException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.updateStatus(999L, Task.TaskStatus.IN_PROGRESS);
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_ExistingTask_DeletesTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository).delete(testTask);
    }

    @Test
    void deleteTask_NonExistingTask_ThrowsEntityNotFoundException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.deleteTask(999L);
        });

        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void addComment_ValidInputs_ReturnsTaskWithNewComment() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Comment comment = Comment.builder()
                .id(1L)
                .content("Test comment")
                .createdAt(LocalDateTime.now())
                .task(testTask)
                .user(testUser)
                .build();

        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.TO_DO)
                .dueDate(testDueDate)
                .priority(1)
                .estimatedHours(8)
                .loggedHours(0)
                .userStory(testUserStory)
                .assignedUser(testUser)
                .comments(List.of(comment))
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // Act
        Task result = taskService.addComment(1L, 1L, "Test comment");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getComments().size());
        assertEquals("Test comment", result.getComments().get(0).getContent());

        verify(commentRepository).save(any(Comment.class));
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void addComment_NonExistingTask_ThrowsEntityNotFoundException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.addComment(999L, 1L, "Test comment");
        });

        verify(commentRepository, never()).save(any(Comment.class));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void addComment_NonExistingUser_ThrowsEntityNotFoundException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.addComment(1L, 999L, "Test comment");
        });

        verify(commentRepository, never()).save(any(Comment.class));
        verify(taskRepository, never()).save(any(Task.class));
    }
}