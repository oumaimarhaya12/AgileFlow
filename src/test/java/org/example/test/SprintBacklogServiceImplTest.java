package org.example.test;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.example.productbacklog.converter.SprintBacklogConverter;
import org.example.productbacklog.dto.SprintBacklogDTO;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.entity.Statut;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.service.impl.SprintBacklogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SprintBacklogServiceImplTest {

    @Mock
    private SprintBacklogRepository sprintBacklogRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private ProductBacklogRepository productBacklogRepository;

    @Spy
    private SprintBacklogConverter sprintBacklogConverter;

    @InjectMocks
    private SprintBacklogServiceImpl sprintBacklogService;

    private SprintBacklog testSprintBacklog;
    private SprintBacklogDTO testSprintBacklogDTO;
    private UserStory testUserStory;
    private Task testTask;
    private ProductBacklog testProductBacklog;

    @BeforeEach
    void setUp() {
        testSprintBacklog = new SprintBacklog();
        testSprintBacklog.setId(1L);
        testSprintBacklog.setTitle("Test Sprint Backlog");
        testSprintBacklog.setUserStories(new ArrayList<>());
        testSprintBacklog.setSprints(new ArrayList<>());

        testSprintBacklogDTO = SprintBacklogDTO.builder()
                .id(1L)
                .title("Test Sprint Backlog")
                .userStoryIds(new ArrayList<>())
                .sprintIds(new ArrayList<>())
                .build();

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

        testProductBacklog = new ProductBacklog();
        testProductBacklog.setId(1);
        testProductBacklog.setTitle("Test Product Backlog");
        testProductBacklog.setSprintBacklogs(new ArrayList<>());

        // Setup the spy converter with lenient() to avoid UnnecessaryStubbingException
        lenient().doReturn(testSprintBacklogDTO).when(sprintBacklogConverter).convertToDTO(testSprintBacklog);
        lenient().doReturn(testSprintBacklog).when(sprintBacklogConverter).convertToEntity(testSprintBacklogDTO);

        List<SprintBacklogDTO> dtoList = Collections.singletonList(testSprintBacklogDTO);
        lenient().doReturn(dtoList).when(sprintBacklogConverter).convertToDTOList(any());
    }

    @Test
    void createSprintBacklog_ValidTitle_ReturnsCreatedSprintBacklog() {
        // Arrange
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprintBacklog);

        // Act
        SprintBacklogDTO result = sprintBacklogService.createSprintBacklog("Test Sprint Backlog");

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
    void createSprintBacklog_WithProductBacklogId_ReturnsSprintBacklogWithProductBacklog() {
        // Arrange
        when(productBacklogRepository.findById(1)).thenReturn(Optional.of(testProductBacklog));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprintBacklog);

        // Act
        SprintBacklogDTO result = sprintBacklogService.createSprintBacklog("Test Sprint Backlog", 1);

        // Assert
        assertNotNull(result);
        verify(productBacklogRepository).save(testProductBacklog);
        verify(sprintBacklogRepository).save(any(SprintBacklog.class));
    }

    @Test
    void createSprintBacklog_WithNonExistingProductBacklogId_ThrowsEntityNotFoundException() {
        // Arrange
        when(productBacklogRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                sprintBacklogService.createSprintBacklog("Test Sprint Backlog", 999));
        verify(sprintBacklogRepository, never()).save(any(SprintBacklog.class));
    }

    @Test
    void getSprintBacklogById_ExistingId_ReturnsSprintBacklog() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        Optional<SprintBacklogDTO> result = sprintBacklogService.getSprintBacklogById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testSprintBacklogDTO, result.get());
    }

    @Test
    void getSprintBacklogById_NonExistingId_ReturnsEmptyOptional() {
        // Arrange
        when(sprintBacklogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<SprintBacklogDTO> result = sprintBacklogService.getSprintBacklogById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getAllSprintBacklogs_ReturnsAllSprintBacklogs() {
        // Arrange
        List<SprintBacklog> sprintBacklogs = Collections.singletonList(testSprintBacklog);
        when(sprintBacklogRepository.findAllByOrderByIdDesc()).thenReturn(sprintBacklogs);

        // Act
        List<SprintBacklogDTO> result = sprintBacklogService.getAllSprintBacklogs();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testSprintBacklogDTO, result.get(0));
    }

    @Test
    void getSprintBacklogsByProductBacklogId_ExistingProductBacklogId_ReturnsSprintBacklogs() {
        // Arrange
        testProductBacklog.getSprintBacklogs().add(testSprintBacklog);
        when(productBacklogRepository.findById(1)).thenReturn(Optional.of(testProductBacklog));

        // Act
        List<SprintBacklogDTO> result = sprintBacklogService.getSprintBacklogsByProductBacklogId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSprintBacklogDTO, result.get(0));
    }

    @Test
    void getSprintBacklogsByProductBacklogId_NonExistingProductBacklogId_ThrowsEntityNotFoundException() {
        // Arrange
        when(productBacklogRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                sprintBacklogService.getSprintBacklogsByProductBacklogId(999));
    }

    @Test
    void updateSprintBacklog_ExistingId_ReturnsUpdatedSprintBacklog() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprintBacklog);

        // Act
        SprintBacklogDTO result = sprintBacklogService.updateSprintBacklog(1L, "Updated Sprint Backlog");

        // Assert
        assertEquals("Test Sprint Backlog", result.getTitle()); // The DTO is mocked to return the test title
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
    void deleteSprintBacklog_WithProductBacklog_DetachesAndDeletesSprintBacklog() {
        // Arrange
        testSprintBacklog.setProductBacklog(testProductBacklog);
        testProductBacklog.getSprintBacklogs().add(testSprintBacklog);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        sprintBacklogService.deleteSprintBacklog(1L);

        // Assert
        verify(productBacklogRepository).save(testProductBacklog);
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
        SprintBacklogDTO result = sprintBacklogService.addUserStoryToSprintBacklog(1L, 1L);

        // Assert
        verify(userStoryRepository).save(testUserStory);
        assertEquals(testSprintBacklogDTO, result);
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
    void removeUserStoryFromSprintBacklog_UserStoryInSprintBacklog_ReturnsUpdatedSprintBacklog() {
        // Arrange
        testUserStory.setSprintBacklog(testSprintBacklog);
        testSprintBacklog.getUserStories().add(testUserStory);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(testUserStory));
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        SprintBacklogDTO result = sprintBacklogService.removeUserStoryFromSprintBacklog(1L, 1L);

        // Assert
        verify(userStoryRepository).save(testUserStory);
        assertEquals(testSprintBacklogDTO, result);
        assertNull(testUserStory.getSprintBacklog());
    }

    @Test
    void removeUserStoryFromSprintBacklog_UserStoryNotInSprintBacklog_ReturnsUnchangedSprintBacklog() {
        // Arrange
        testUserStory.setSprintBacklog(null);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(testUserStory));
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        SprintBacklogDTO result = sprintBacklogService.removeUserStoryFromSprintBacklog(1L, 1L);

        // Assert
        verify(userStoryRepository, never()).save(any(UserStory.class));
        assertEquals(testSprintBacklogDTO, result);
    }

    @Test
    void getUserStoriesInSprintBacklog_ReturnsUserStories() {
        // Arrange
        testSprintBacklog.getUserStories().add(testUserStory);
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        List<UserStory> result = sprintBacklogService.getUserStoriesInSprintBacklog(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUserStory, result.get(0));
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

    @Test
    void getUserStoryStatusSummary_ReturnsCorrectCounts() {
        // Arrange
        testSprintBacklog.getUserStories().add(testUserStory);
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        Map<Statut, Long> statusSummary = sprintBacklogService.getUserStoryStatusSummary(1L);

        // Assert
        assertEquals(1L, statusSummary.get(Statut.toDo));
        assertEquals(0L, statusSummary.get(Statut.blocked));
        assertEquals(0L, statusSummary.get(Statut.finished));
        assertEquals(0L, statusSummary.get(Statut.inProgress));
        assertEquals(0L, statusSummary.get(Statut.inTesting));
    }

    @Test
    void getTasksByStatus_ReturnsTasksWithSpecificStatus() {
        // Arrange
        testSprintBacklog.getUserStories().add(testUserStory);
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        List<Task> result = sprintBacklogService.getTasksByStatus(1L, Task.TaskStatus.TO_DO);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTask, result.get(0));
    }

    @Test
    void countTotalUserStories_ReturnsCorrectCount() {
        // Arrange
        when(sprintBacklogRepository.countUserStoriesBySprintBacklogId(1L)).thenReturn(5);

        // Act
        int count = sprintBacklogService.countTotalUserStories(1L);

        // Assert
        assertEquals(5, count);
    }

    @Test
    void countTotalTasks_ReturnsCorrectCount() {
        // Arrange
        testSprintBacklog.getUserStories().add(testUserStory);
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        int count = sprintBacklogService.countTotalTasks(1L);

        // Assert
        assertEquals(1, count);
    }

    @Test
    void assignSprintBacklogToProductBacklog_ValidIds_ReturnsUpdatedSprintBacklog() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(productBacklogRepository.findById(1)).thenReturn(Optional.of(testProductBacklog));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprintBacklog);

        // Act
        SprintBacklogDTO result = sprintBacklogService.assignSprintBacklogToProductBacklog(1L, 1);

        // Assert
        assertNotNull(result);
        verify(productBacklogRepository).save(testProductBacklog);
        verify(sprintBacklogRepository).save(testSprintBacklog);
    }

    @Test
    void assignSprintBacklogToProductBacklog_WithPreviousProductBacklog_DetachesAndAssigns() {
        // Arrange
        ProductBacklog previousProductBacklog = new ProductBacklog();
        previousProductBacklog.setId(2);
        previousProductBacklog.setTitle("Previous Product Backlog");
        previousProductBacklog.setSprintBacklogs(new ArrayList<>(Arrays.asList(testSprintBacklog)));

        testSprintBacklog.setProductBacklog(previousProductBacklog);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(productBacklogRepository.findById(1)).thenReturn(Optional.of(testProductBacklog));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprintBacklog);

        // Act
        SprintBacklogDTO result = sprintBacklogService.assignSprintBacklogToProductBacklog(1L, 1);

        // Assert
        assertNotNull(result);
        verify(productBacklogRepository).save(previousProductBacklog);
        verify(productBacklogRepository).save(testProductBacklog);
        verify(sprintBacklogRepository).save(testSprintBacklog);
    }

    @Test
    void assignSprintBacklogToProductBacklog_NonExistingSprintBacklogId_ThrowsEntityNotFoundException() {
        // Arrange
        when(sprintBacklogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                sprintBacklogService.assignSprintBacklogToProductBacklog(999L, 1));
        verify(productBacklogRepository, never()).save(any(ProductBacklog.class));
    }

    @Test
    void assignSprintBacklogToProductBacklog_NonExistingProductBacklogId_ThrowsEntityNotFoundException() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(productBacklogRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                sprintBacklogService.assignSprintBacklogToProductBacklog(1L, 999));
        verify(sprintBacklogRepository, never()).save(any(SprintBacklog.class));
    }

    @Test
    void removeSprintBacklogFromProductBacklog_SprintBacklogWithProductBacklog_ReturnsUpdatedSprintBacklog() {
        // Arrange
        testSprintBacklog.setProductBacklog(testProductBacklog);
        testProductBacklog.getSprintBacklogs().add(testSprintBacklog);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprintBacklog);

        // Act
        SprintBacklogDTO result = sprintBacklogService.removeSprintBacklogFromProductBacklog(1L);

        // Assert
        assertNotNull(result);
        verify(productBacklogRepository).save(testProductBacklog);
        verify(sprintBacklogRepository).save(testSprintBacklog);
    }

    @Test
    void removeSprintBacklogFromProductBacklog_SprintBacklogWithoutProductBacklog_ReturnsUnchangedSprintBacklog() {
        // Arrange
        testSprintBacklog.setProductBacklog(null);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));

        // Act
        SprintBacklogDTO result = sprintBacklogService.removeSprintBacklogFromProductBacklog(1L);

        // Assert
        assertNotNull(result);
        verify(productBacklogRepository, never()).save(any(ProductBacklog.class));
        verify(sprintBacklogRepository, never()).save(any(SprintBacklog.class));
    }
}