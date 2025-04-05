package org.example.test;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import org.example.productbacklog.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private CommentRepository commentRepository;

    @Spy
    private TaskConverter taskConverter;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User testUser;
    private UserStory testUserStory;
    private Task testTask;
    private TaskDTO testTaskDTO;
    private LocalDateTime testDueDate;

    @BeforeEach
    void setUp() {
        testDueDate = LocalDateTime.now().plusDays(7);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

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

        testTaskDTO = new TaskDTO();
        testTaskDTO.setId(1L);
        testTaskDTO.setTitle("Test Task");
        testTaskDTO.setDescription("Test Description");
        testTaskDTO.setStatus(Task.TaskStatus.TO_DO.name());
        testTaskDTO.setDueDate(testDueDate);
        testTaskDTO.setPriority(1);
        testTaskDTO.setEstimatedHours(8);
        testTaskDTO.setLoggedHours(0);
        testTaskDTO.setUserStoryId(1L);
        testTaskDTO.setAssignedUserId(1L);

        lenient().doReturn(testTaskDTO).when(taskConverter).convertToDTO(testTask);
        lenient().doReturn(testTask).when(taskConverter).convertToEntity(testTaskDTO);

        List<TaskDTO> dtoList = List.of(testTaskDTO);
        lenient().doReturn(dtoList).when(taskConverter).convertToDTOList(any());

        // Fix for empty list tests
        lenient().doReturn(Collections.emptyList()).when(taskConverter).convertToDTOList(Collections.emptyList());
    }

    @Test
    void createTask_ValidInputs_ReturnsCreatedTask() {
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(testUserStory));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO createdTask = taskService.createTask(
                "Test Task",
                "Test Description",
                Task.TaskStatus.TO_DO,
                testDueDate,
                1,
                8,
                1L,
                1L
        );

        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        assertEquals("Test Description", createdTask.getDescription());
        assertEquals(Task.TaskStatus.TO_DO.name(), createdTask.getStatus());
        assertEquals(testDueDate, createdTask.getDueDate());
        assertEquals(1, createdTask.getPriority());
        assertEquals(8, createdTask.getEstimatedHours());
        assertEquals(0, createdTask.getLoggedHours());
        assertEquals(1L, createdTask.getUserStoryId());
        assertEquals(1L, createdTask.getAssignedUserId());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WithInvalidUserStory_ThrowsEntityNotFoundException() {
        when(userStoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.createTask(
                    "Test Task",
                    "Test Description",
                    Task.TaskStatus.TO_DO,
                    testDueDate,
                    1,
                    8,
                    999L,
                    null
            );
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_WithInvalidUser_ThrowsEntityNotFoundException() {
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(testUserStory));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.createTask(
                    "Test Task",
                    "Test Description",
                    Task.TaskStatus.TO_DO,
                    testDueDate,
                    1,
                    8,
                    1L,
                    999L
            );
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_WithNullUserStoryAndUser_ReturnsCreatedTask() {
        Task taskWithoutRelations = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.TO_DO)
                .dueDate(testDueDate)
                .priority(1)
                .estimatedHours(8)
                .loggedHours(0)
                .userStory(null)
                .assignedUser(null)
                .comments(new ArrayList<>())
                .build();

        TaskDTO taskDTOWithoutRelations = new TaskDTO();
        taskDTOWithoutRelations.setId(1L);
        taskDTOWithoutRelations.setTitle("Test Task");
        taskDTOWithoutRelations.setDescription("Test Description");
        taskDTOWithoutRelations.setStatus(Task.TaskStatus.TO_DO.name());
        taskDTOWithoutRelations.setDueDate(testDueDate);
        taskDTOWithoutRelations.setPriority(1);
        taskDTOWithoutRelations.setEstimatedHours(8);
        taskDTOWithoutRelations.setLoggedHours(0);
        taskDTOWithoutRelations.setUserStoryId(null);
        taskDTOWithoutRelations.setAssignedUserId(null);

        when(taskRepository.save(any(Task.class))).thenReturn(taskWithoutRelations);
        when(taskConverter.convertToDTO(taskWithoutRelations)).thenReturn(taskDTOWithoutRelations);

        TaskDTO createdTask = taskService.createTask(
                "Test Task",
                "Test Description",
                Task.TaskStatus.TO_DO,
                testDueDate,
                1,
                8,
                null,
                null
        );

        assertNotNull(createdTask);
        assertNull(createdTask.getUserStoryId());
        assertNull(createdTask.getAssignedUserId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WithNullStatus_UsesToDoAsDefault() {
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(testUserStory));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO createdTask = taskService.createTask(
                "Test Task",
                "Test Description",
                null,
                testDueDate,
                1,
                8,
                1L,
                1L
        );

        assertNotNull(createdTask);
        assertEquals(Task.TaskStatus.TO_DO.name(), createdTask.getStatus());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertEquals(Task.TaskStatus.TO_DO, taskCaptor.getValue().getStatus());
    }

    @Test
    void findById_ExistingId_ReturnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Optional<TaskDTO> result = taskService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTaskDTO, result.get());
    }

    @Test
    void findById_NonExistingId_ReturnsEmptyOptional() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<TaskDTO> result = taskService.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ReturnsList() {
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findAll()).thenReturn(tasks);

        List<TaskDTO> result = taskService.findAll();

        assertEquals(1, result.size());
        assertEquals(testTaskDTO, result.get(0));
    }

    @Test
    void findAll_EmptyRepository_ReturnsEmptyList() {
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        List<TaskDTO> result = taskService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserStoryId_ReturnsList() {
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findByUserStoryId(1L)).thenReturn(tasks);

        List<TaskDTO> result = taskService.findByUserStoryId(1L);

        assertEquals(1, result.size());
        assertEquals(testTaskDTO, result.get(0));
    }

    @Test
    void findByUserStoryId_NoTasksFound_ReturnsEmptyList() {
        when(taskRepository.findByUserStoryId(1L)).thenReturn(Collections.emptyList());

        List<TaskDTO> result = taskService.findByUserStoryId(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByAssignedUserId_ReturnsList() {
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findByAssignedUserId(1L)).thenReturn(tasks);

        List<TaskDTO> result = taskService.findByAssignedUserId(1L);

        assertEquals(1, result.size());
        assertEquals(testTaskDTO, result.get(0));
    }

    @Test
    void findByAssignedUserId_NoTasksFound_ReturnsEmptyList() {
        when(taskRepository.findByAssignedUserId(1L)).thenReturn(Collections.emptyList());

        List<TaskDTO> result = taskService.findByAssignedUserId(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByStatus_ReturnsList() {
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findByStatus(Task.TaskStatus.TO_DO)).thenReturn(tasks);

        List<TaskDTO> result = taskService.findByStatus(Task.TaskStatus.TO_DO);

        assertEquals(1, result.size());
        assertEquals(testTaskDTO, result.get(0));
    }

    @Test
    void findByStatus_NoTasksFound_ReturnsEmptyList() {
        when(taskRepository.findByStatus(Task.TaskStatus.FINISHED)).thenReturn(Collections.emptyList());

        List<TaskDTO> result = taskService.findByStatus(Task.TaskStatus.FINISHED);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateTask_ExistingTask_ReturnsUpdatedTask() {
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

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setId(1L);
        updatedTaskDTO.setTitle("Updated Task");
        updatedTaskDTO.setDescription("Updated Description");
        updatedTaskDTO.setStatus(Task.TaskStatus.IN_PROGRESS.name());
        updatedTaskDTO.setDueDate(testDueDate.plusDays(1));
        updatedTaskDTO.setPriority(2);
        updatedTaskDTO.setEstimatedHours(16);
        updatedTaskDTO.setLoggedHours(0);
        updatedTaskDTO.setUserStoryId(1L);
        updatedTaskDTO.setAssignedUserId(1L);

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskConverter.convertToDTO(updatedTask)).thenReturn(updatedTaskDTO);

        TaskDTO result = taskService.updateTask(
                1L,
                "Updated Task",
                "Updated Description",
                Task.TaskStatus.IN_PROGRESS,
                testDueDate.plusDays(1),
                2,
                16
        );

        assertNotNull(result);
        assertEquals("Updated Task", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(Task.TaskStatus.IN_PROGRESS.name(), result.getStatus());
        assertEquals(testDueDate.plusDays(1), result.getDueDate());
        assertEquals(2, result.getPriority());
        assertEquals(16, result.getEstimatedHours());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_PartialUpdate_OnlyUpdatesProvidedFields() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO result = taskService.updateTask(
                1L,
                "Updated Title",
                null,
                null,
                null,
                0,
                0
        );

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();

        assertEquals("Updated Title", capturedTask.getTitle());
        assertEquals("Test Description", capturedTask.getDescription());
        assertEquals(Task.TaskStatus.TO_DO, capturedTask.getStatus());
        assertEquals(testDueDate, capturedTask.getDueDate());
        assertEquals(1, capturedTask.getPriority());
        assertEquals(8, capturedTask.getEstimatedHours());
    }

    @Test
    void updateTask_NonExistingTask_ThrowsEntityNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

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

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setId(1L);
        updatedTaskDTO.setTitle("Test Task");
        updatedTaskDTO.setDescription("Test Description");
        updatedTaskDTO.setStatus(Task.TaskStatus.TO_DO.name());
        updatedTaskDTO.setDueDate(testDueDate);
        updatedTaskDTO.setPriority(1);
        updatedTaskDTO.setEstimatedHours(8);
        updatedTaskDTO.setLoggedHours(0);
        updatedTaskDTO.setUserStoryId(1L);
        updatedTaskDTO.setAssignedUserId(2L);

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskConverter.convertToDTO(updatedTask)).thenReturn(updatedTaskDTO);

        TaskDTO result = taskService.assignTaskToUser(1L, 2L);

        assertNotNull(result);
        assertEquals(2L, result.getAssignedUserId());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void assignTaskToUser_NonExistingTask_ThrowsEntityNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.assignTaskToUser(999L, 1L);
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void assignTaskToUser_NonExistingUser_ThrowsEntityNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.assignTaskToUser(1L, 999L);
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void logHours_ExistingTask_ReturnsUpdatedTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.TO_DO)
                .dueDate(testDueDate)
                .priority(1)
                .estimatedHours(8)
                .loggedHours(4)
                .userStory(testUserStory)
                .assignedUser(testUser)
                .build();

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setId(1L);
        updatedTaskDTO.setTitle("Test Task");
        updatedTaskDTO.setDescription("Test Description");
        updatedTaskDTO.setStatus(Task.TaskStatus.TO_DO.name());
        updatedTaskDTO.setDueDate(testDueDate);
        updatedTaskDTO.setPriority(1);
        updatedTaskDTO.setEstimatedHours(8);
        updatedTaskDTO.setLoggedHours(4);
        updatedTaskDTO.setUserStoryId(1L);
        updatedTaskDTO.setAssignedUserId(1L);

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskConverter.convertToDTO(updatedTask)).thenReturn(updatedTaskDTO);

        TaskDTO result = taskService.logHours(1L, 4);

        assertNotNull(result);
        assertEquals(4, result.getLoggedHours());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void logHours_MultipleLoggings_AccumulatesHours() {
        Task taskWithLoggedHours = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.TO_DO)
                .dueDate(testDueDate)
                .priority(1)
                .estimatedHours(8)
                .loggedHours(3)
                .userStory(testUserStory)
                .assignedUser(testUser)
                .comments(new ArrayList<>())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskWithLoggedHours));

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.TO_DO)
                .dueDate(testDueDate)
                .priority(1)
                .estimatedHours(8)
                .loggedHours(7)
                .userStory(testUserStory)
                .assignedUser(testUser)
                .build();

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setId(1L);
        updatedTaskDTO.setTitle("Test Task");
        updatedTaskDTO.setDescription("Test Description");
        updatedTaskDTO.setStatus(Task.TaskStatus.TO_DO.name());
        updatedTaskDTO.setDueDate(testDueDate);
        updatedTaskDTO.setPriority(1);
        updatedTaskDTO.setEstimatedHours(8);
        updatedTaskDTO.setLoggedHours(7);
        updatedTaskDTO.setUserStoryId(1L);
        updatedTaskDTO.setAssignedUserId(1L);

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskConverter.convertToDTO(updatedTask)).thenReturn(updatedTaskDTO);

        TaskDTO result = taskService.logHours(1L, 4);

        assertNotNull(result);
        assertEquals(7, result.getLoggedHours());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertEquals(7, taskCaptor.getValue().getLoggedHours());
    }

    @Test
    void logHours_NonExistingTask_ThrowsEntityNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.logHours(999L, 4);
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void logHours_NegativeHours_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.logHours(1L, -4);
        });

        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void logHours_ZeroHours_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.logHours(1L, 0);
        });

        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateStatus_ExistingTask_ReturnsUpdatedTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.IN_PROGRESS)
                .dueDate(testDueDate)
                .priority(1)
                .estimatedHours(8)
                .loggedHours(0)
                .userStory(testUserStory)
                .assignedUser(testUser)
                .build();

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setId(1L);
        updatedTaskDTO.setTitle("Test Task");
        updatedTaskDTO.setDescription("Test Description");
        updatedTaskDTO.setStatus(Task.TaskStatus.IN_PROGRESS.name());
        updatedTaskDTO.setDueDate(testDueDate);
        updatedTaskDTO.setPriority(1);
        updatedTaskDTO.setEstimatedHours(8);
        updatedTaskDTO.setLoggedHours(0);
        updatedTaskDTO.setUserStoryId(1L);
        updatedTaskDTO.setAssignedUserId(1L);

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskConverter.convertToDTO(updatedTask)).thenReturn(updatedTaskDTO);

        TaskDTO result = taskService.updateStatus(1L, Task.TaskStatus.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(Task.TaskStatus.IN_PROGRESS.name(), result.getStatus());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateStatus_AllStatusValues_WorksCorrectly() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        for (Task.TaskStatus status : Task.TaskStatus.values()) {
            TaskDTO updatedTaskDTO = new TaskDTO();
            updatedTaskDTO.setStatus(status.name());
            when(taskConverter.convertToDTO(any(Task.class))).thenReturn(updatedTaskDTO);

            TaskDTO result = taskService.updateStatus(1L, status);
            assertEquals(status.name(), result.getStatus());
        }

        verify(taskRepository, times(Task.TaskStatus.values().length)).save(any(Task.class));
    }

    @Test
    void updateStatus_NonExistingTask_ThrowsEntityNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.updateStatus(999L, Task.TaskStatus.IN_PROGRESS);
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_ExistingTask_DeletesTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(testTask);
    }

    @Test
    void deleteTask_NonExistingTask_ThrowsEntityNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.deleteTask(999L);
        });

        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void addComment_ValidInputs_ReturnsTaskWithNewComment() {
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

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setId(1L);
        updatedTaskDTO.setTitle("Test Task");
        updatedTaskDTO.setDescription("Test Description");
        updatedTaskDTO.setStatus(Task.TaskStatus.TO_DO.name());
        updatedTaskDTO.setDueDate(testDueDate);
        updatedTaskDTO.setPriority(1);
        updatedTaskDTO.setEstimatedHours(8);
        updatedTaskDTO.setLoggedHours(0);
        updatedTaskDTO.setUserStoryId(1L);
        updatedTaskDTO.setAssignedUserId(1L);

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskConverter.convertToDTO(updatedTask)).thenReturn(updatedTaskDTO);

        TaskDTO result = taskService.addComment(1L, 1L, "Test comment");

        assertNotNull(result);
        verify(commentRepository).save(any(Comment.class));
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void addComment_NonExistingTask_ThrowsEntityNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.addComment(999L, 1L, "Test comment");
        });

        verify(commentRepository, never()).save(any(Comment.class));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void addComment_NonExistingUser_ThrowsEntityNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.addComment(1L, 999L, "Test comment");
        });

        verify(commentRepository, never()).save(any(Comment.class));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void addComment_MultipleComments_AddsAllComments() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Comment comment1 = Comment.builder()
                .id(1L)
                .content("First comment")
                .createdAt(LocalDateTime.now())
                .task(testTask)
                .user(testUser)
                .build();

        Comment comment2 = Comment.builder()
                .id(2L)
                .content("Second comment")
                .createdAt(LocalDateTime.now())
                .task(testTask)
                .user(testUser)
                .build();

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment1)
                .thenReturn(comment2);

        Task taskWithOneComment = Task.builder()
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
                .comments(List.of(comment1))
                .build();

        Task taskWithTwoComments = Task.builder()
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
                .comments(Arrays.asList(comment1, comment2))
                .build();

        when(taskRepository.save(any(Task.class)))
                .thenReturn(taskWithOneComment)
                .thenReturn(taskWithTwoComments);

        when(taskConverter.convertToDTO(any(Task.class))).thenReturn(testTaskDTO);

        taskService.addComment(1L, 1L, "First comment");
        taskService.addComment(1L, 1L, "Second comment");

        verify(commentRepository, times(2)).save(any(Comment.class));
        verify(taskRepository, times(2)).save(any(Task.class));
    }
}