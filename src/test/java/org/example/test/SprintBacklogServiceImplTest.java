package org.example.test;

import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.entity.Statut;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.service.impl.SprintBacklogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SprintBacklogServiceImplTest {

    @Mock
    private SprintBacklogRepository sprintBacklogRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @InjectMocks
    private SprintBacklogServiceImpl sprintBacklogService;

    private SprintBacklog testSprintBacklog;
    private UserStory testUserStory;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testSprintBacklog = new SprintBacklog();
        testSprintBacklog.setId(1L);
        testSprintBacklog.setTitle("Test Sprint Backlog");
        testSprintBacklog.setUserStories(new ArrayList<>());

        testUserStory = UserStory.builder()
                .id(1L)
                .title("Test User Story")
                .status(Statut.toDo)
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .status(Task.TaskStatus.TO_DO)
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();

        List<Task> tasks = new ArrayList<>();
        tasks.add(testTask);
        testUserStory.setTasks(tasks);
    }

    @Test
    void createSprintBacklog_ValidTitle_ReturnsCreatedSprintBacklog() {
        // Arrange
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprintBacklog);

        // Act
        SprintBacklog result = sprintBacklogService.createSprintBacklog("Test Sprint Backlog");

        // Assert
        assertNotNull(result);
        assertEquals("Test Sprint Backlog", result.getTitle());
        verify(sprintBacklogRepository).save(any(SprintBacklog.class));
    }

    @Test
    void createSprintBacklog_EmptyTitle_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> sprintBacklogService.createSprintBacklog(""));
        verify(sprintBacklogRepository, never()).save(any(SprintBacklog.class));
    }

    @Test
    void getSprintBacklogById_ExistingId_ReturnsSprintBacklog() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        Optional<SprintBacklog> result = sprintBacklogService.getSprintBacklogById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testSprintBacklog, result.get());
    }

    @Test
    void getSprintBacklogById_NonExistingId_ReturnsEmptyOptional() {
        // Arrange
        when(sprintBacklogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<SprintBacklog> result = sprintBacklogService.getSprintBacklogById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getAllSprintBacklogs_ReturnsAllSprintBacklogs() {
        // Arrange
        List<SprintBacklog> sprintBacklogs = Collections.singletonList(testSprintBacklog);
        when(sprintBacklogRepository.findAllByOrderByIdDesc()).thenReturn(sprintBacklogs);

        // Act
        List<SprintBacklog> result = sprintBacklogService.getAllSprintBacklogs();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testSprintBacklog, result.get(0));
    }

    @Test
    void updateSprintBacklog_ExistingId_ReturnsUpdatedSprintBacklog() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprintBacklog);

        // Act
        SprintBacklog result = sprintBacklogService.updateSprintBacklog(1L, "Updated Sprint Backlog");

        // Assert
        assertEquals("Updated Sprint Backlog", result.getTitle());
        verify(sprintBacklogRepository).save(testSprintBacklog);
    }

    @Test
    void updateSprintBacklog_NonExistingId_ThrowsEntityNotFoundException() {
        // Arrange
        when(sprintBacklogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> sprintBacklogService.updateSprintBacklog(999L, "Updated Sprint Backlog"));
        verify(sprintBacklogRepository, never()).save(any(SprintBacklog.class));
    }

    @Test
    void deleteSprintBacklog_ExistingId_DeletesSprintBacklog() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        sprintBacklogService.deleteSprintBacklog(1L);

        // Assert
        verify(sprintBacklogRepository).delete(testSprintBacklog);
    }

    @Test
    void deleteSprintBacklog_NonExistingId_ThrowsEntityNotFoundException() {
        // Arrange
        when(sprintBacklogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> sprintBacklogService.deleteSprintBacklog(999L));
        verify(sprintBacklogRepository, never()).delete(any(SprintBacklog.class));
    }

    @Test
    void addUserStoryToSprintBacklog_ValidIds_ReturnsUpdatedSprintBacklog() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(testUserStory));
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        SprintBacklog result = sprintBacklogService.addUserStoryToSprintBacklog(1L, 1L);

        // Assert
        verify(userStoryRepository).save(testUserStory);
        assertEquals(testSprintBacklog, result);
    }

    @Test
    void addUserStoryToSprintBacklog_NonExistingSprintBacklogId_ThrowsEntityNotFoundException() {
        // Arrange
        when(sprintBacklogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> sprintBacklogService.addUserStoryToSprintBacklog(999L, 1L));
        verify(userStoryRepository, never()).save(any(UserStory.class));
    }

    @Test
    void addUserStoryToSprintBacklog_NonExistingUserStoryId_ThrowsEntityNotFoundException() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(userStoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> sprintBacklogService.addUserStoryToSprintBacklog(1L, 999L));
        verify(userStoryRepository, never()).save(any(UserStory.class));
    }

    @Test
    void calculateSprintProgress_NoTasks_ReturnsZero() {
        // Arrange
        testSprintBacklog.getUserStories().clear();
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        double progress = sprintBacklogService.calculateSprintProgress(1L);

        // Assert
        assertEquals(0.0, progress);
    }

    @Test
    void calculateSprintProgress_WithTasks_ReturnsPercentage() {
        // Arrange
        testUserStory.getTasks().get(0).setStatus(Task.TaskStatus.FINISHED);
        testSprintBacklog.getUserStories().add(testUserStory);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        double progress = sprintBacklogService.calculateSprintProgress(1L);

        // Assert
        assertEquals(100.0, progress);
    }

    @Test
    void getTaskStatusSummary_ReturnsCorrectCounts() {
        // Arrange
        testSprintBacklog.getUserStories().add(testUserStory);
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        Map<Task.TaskStatus, Long> statusSummary = sprintBacklogService.getTaskStatusSummary(1L);

        // Assert
        assertEquals(1L, statusSummary.get(Task.TaskStatus.TO_DO));
        assertEquals(0L, statusSummary.get(Task.TaskStatus.IN_PROGRESS));
        assertEquals(0L, statusSummary.get(Task.TaskStatus.FINISHED));
    }
}
