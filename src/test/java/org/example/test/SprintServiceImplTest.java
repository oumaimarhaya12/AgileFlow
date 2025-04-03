package org.example.test;

import org.example.productbacklog.entity.Sprint;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.example.productbacklog.repository.SprintRepository;
import org.example.productbacklog.service.impl.SprintServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SprintServiceImplTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private SprintBacklogRepository sprintBacklogRepository;

    @InjectMocks
    private SprintServiceImpl sprintService;

    private Sprint testSprint;
    private SprintBacklog testSprintBacklog;
    private LocalDate today;
    private LocalDate tomorrow;
    private LocalDate nextWeek;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
        nextWeek = today.plusWeeks(1);

        testSprintBacklog = new SprintBacklog();
        testSprintBacklog.setId(1L);

        testSprint = Sprint.builder()
                .id(1L)
                .name("Test Sprint")
                .startDate(today)
                .endDate(nextWeek)
                .sprintBacklog(testSprintBacklog)
                .build();
    }

    @Test
    void createSprint_WithValidInputs_ShouldCreateSprint() {
        // Arrange
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.of(testSprintBacklog));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());

        // Act
        Sprint result = sprintService.createSprint("Test Sprint", today, nextWeek, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Sprint", result.getName());
        assertEquals(today, result.getStartDate());
        assertEquals(nextWeek, result.getEndDate());
        assertEquals(testSprintBacklog, result.getSprintBacklog());
        verify(sprintRepository).save(any(Sprint.class));
    }

    @Test
    void createSprint_WithEmptyName_ShouldThrowException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.createSprint("", today, nextWeek, 1L)
        );
        assertEquals("Sprint name cannot be empty", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void createSprint_WithNullDates_ShouldThrowException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.createSprint("Test Sprint", null, nextWeek, 1L)
        );
        assertEquals("Sprint start date and end date cannot be null", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.createSprint("Test Sprint", today, null, 1L)
        );
        assertEquals("Sprint start date and end date cannot be null", exception.getMessage());

        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void createSprint_WithInvalidDateRange_ShouldThrowException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.createSprint("Test Sprint", nextWeek, today, 1L)
        );
        assertEquals("Sprint end date must be after start date", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void createSprint_WithNonExistentSprintBacklog_ShouldThrowException() {
        // Arrange
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                sprintService.createSprint("Test Sprint", today, nextWeek, 999L)
        );
        assertEquals("Sprint Backlog not found with ID: 999", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void createSprint_WithOverlappingDates_ShouldThrowException() {
        // Arrange
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.of(testSprintBacklog));

        // Create an existing sprint with overlapping dates
        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(tomorrow)
                .endDate(nextWeek.plusDays(1))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(List.of(existingSprint));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.createSprint("Test Sprint", today, nextWeek, 1L)
        );
        assertEquals("Sprint dates overlap with an existing sprint in the same sprint backlog", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void getSprintById_WithExistingId_ShouldReturnSprint() {
        // Arrange
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));

        // Act
        Optional<Sprint> result = sprintService.getSprintById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testSprint, result.get());
    }

    @Test
    void getSprintById_WithNonExistentId_ShouldReturnEmpty() {
        // Arrange
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        Optional<Sprint> result = sprintService.getSprintById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getAllSprints_ShouldReturnAllSprints() {
        // Arrange
        Sprint sprint2 = Sprint.builder()
                .id(2L)
                .name("Sprint 2")
                .startDate(nextWeek)
                .endDate(nextWeek.plusWeeks(1))
                .sprintBacklog(testSprintBacklog)
                .build();

        List<Sprint> expectedSprints = Arrays.asList(testSprint, sprint2);
        when(sprintRepository.findAll()).thenReturn(expectedSprints);

        // Act
        List<Sprint> result = sprintService.getAllSprints();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedSprints, result);
    }

    @Test
    void updateSprint_WithValidInputs_ShouldUpdateSprint() {
        // Arrange
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());

        LocalDate newStartDate = today.plusDays(2);
        LocalDate newEndDate = nextWeek.plusDays(2);
        String newName = "Updated Sprint";

        // Act
        Sprint result = sprintService.updateSprint(1L, newName, newStartDate, newEndDate);

        // Assert
        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(newStartDate, result.getStartDate());
        assertEquals(newEndDate, result.getEndDate());
        verify(sprintRepository).save(testSprint);
    }

    @Test
    void updateSprint_WithOnlyStartDate_ShouldUpdateStartDate() {
        // Arrange
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());

        LocalDate newStartDate = today.plusDays(2);

        // Act
        Sprint result = sprintService.updateSprint(1L, null, newStartDate, null);

        // Assert
        assertNotNull(result);
        assertEquals(newStartDate, result.getStartDate());
        assertEquals(nextWeek, result.getEndDate()); // End date should remain unchanged
        verify(sprintRepository).save(testSprint);
    }

    @Test
    void updateSprint_WithOnlyEndDate_ShouldUpdateEndDate() {
        // Arrange
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());

        LocalDate newEndDate = nextWeek.plusDays(2);

        // Act
        Sprint result = sprintService.updateSprint(1L, null, null, newEndDate);

        // Assert
        assertNotNull(result);
        assertEquals(today, result.getStartDate()); // Start date should remain unchanged
        assertEquals(newEndDate, result.getEndDate());
        verify(sprintRepository).save(testSprint);
    }

    @Test
    void updateSprint_WithInvalidDateRange_ShouldThrowException() {
        // Arrange
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.updateSprint(1L, null, nextWeek, today)
        );
        assertEquals("Sprint end date must be after start date", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void deleteSprint_WithExistingId_ShouldDeleteSprint() {
        // Arrange
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));
        doNothing().when(sprintRepository).delete(any(Sprint.class));

        // Act
        sprintService.deleteSprint(1L);

        // Assert
        verify(sprintRepository).delete(testSprint);
    }

    @Test
    void deleteSprint_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                sprintService.deleteSprint(999L)
        );
        assertEquals("Sprint not found with ID: 999", exception.getMessage());
        verify(sprintRepository, never()).delete(any(Sprint.class));
    }

    @Test
    void getSprintsBySprintBacklogId_WithValidId_ShouldReturnSprints() {
        // Arrange
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.of(testSprintBacklog));
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(List.of(testSprint));

        // Act
        List<Sprint> result = sprintService.getSprintsBySprintBacklogId(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testSprint, result.get(0));
    }

    @Test
    void getSprintsBySprintBacklogId_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                sprintService.getSprintsBySprintBacklogId(999L)
        );
        assertEquals("Sprint Backlog not found with ID: 999", exception.getMessage());
    }

    @Test
    void getActiveSprintsByDate_ShouldReturnActiveSprints() {
        // Arrange
        when(sprintRepository.findActiveSprintsByDate(any(LocalDate.class))).thenReturn(List.of(testSprint));

        // Act
        List<Sprint> result = sprintService.getActiveSprintsByDate(today);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testSprint, result.get(0));
    }

    @Test
    void getUpcomingSprints_ShouldReturnUpcomingSprints() {
        // Arrange
        Sprint upcomingSprint = Sprint.builder()
                .id(2L)
                .name("Upcoming Sprint")
                .startDate(nextWeek)
                .endDate(nextWeek.plusWeeks(1))
                .build();

        when(sprintRepository.findUpcomingSprints(any(LocalDate.class))).thenReturn(List.of(upcomingSprint));

        // Act
        List<Sprint> result = sprintService.getUpcomingSprints();

        // Assert
        assertEquals(1, result.size());
        assertEquals(upcomingSprint, result.get(0));
    }

    @Test
    void getCompletedSprints_ShouldReturnCompletedSprints() {
        // Arrange
        Sprint completedSprint = Sprint.builder()
                .id(3L)
                .name("Completed Sprint")
                .startDate(today.minusWeeks(2))
                .endDate(today.minusWeeks(1))
                .build();

        when(sprintRepository.findCompletedSprints(any(LocalDate.class))).thenReturn(List.of(completedSprint));

        // Act
        List<Sprint> result = sprintService.getCompletedSprints();

        // Assert
        assertEquals(1, result.size());
        assertEquals(completedSprint, result.get(0));
    }

    @Test
    void assignSprintToSprintBacklog_WithValidIds_ShouldAssignSprint() {
        // Arrange
        SprintBacklog newSprintBacklog = new SprintBacklog();
        newSprintBacklog.setId(2L);

        Sprint sprintWithoutBacklog = Sprint.builder()
                .id(1L)
                .name("Test Sprint")
                .startDate(today)
                .endDate(nextWeek)
                .sprintBacklog(null)
                .build();

        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(sprintWithoutBacklog));
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.of(newSprintBacklog));
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());
        when(sprintRepository.save(any(Sprint.class))).thenReturn(sprintWithoutBacklog);

        // Act
        Sprint result = sprintService.assignSprintToSprintBacklog(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(newSprintBacklog, result.getSprintBacklog());
        verify(sprintRepository).save(sprintWithoutBacklog);
    }

    @Test
    void removeSprintFromSprintBacklog_WithValidId_ShouldRemoveBacklog() {
        // Arrange
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);

        // Act
        Sprint result = sprintService.removeSprintFromSprintBacklog(1L);

        // Assert
        assertNotNull(result);
        assertNull(result.getSprintBacklog());
        verify(sprintRepository).save(testSprint);
    }

    @Test
    void isSprintDateRangeValid_WithValidRange_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(sprintService.isSprintDateRangeValid(today, tomorrow));
        assertTrue(sprintService.isSprintDateRangeValid(today, today)); // Equal dates should be valid
    }

    @Test
    void isSprintDateRangeValid_WithInvalidRange_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(sprintService.isSprintDateRangeValid(tomorrow, today));
        assertFalse(sprintService.isSprintDateRangeValid(null, today));
        assertFalse(sprintService.isSprintDateRangeValid(today, null));
    }

    @Test
    void isSprintOverlapping_WithOverlappingSprints_ShouldReturnTrue() {
        // Arrange
        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(tomorrow)
                .endDate(nextWeek.plusDays(1))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(List.of(existingSprint));

        // Act & Assert
        // Sprint that starts during an existing one
        assertTrue(sprintService.isSprintOverlapping(1L, tomorrow.plusDays(1), nextWeek.plusDays(2), null));

        // Sprint that ends during an existing one
        assertTrue(sprintService.isSprintOverlapping(1L, today, tomorrow.plusDays(1), null));

        // Sprint that completely contains an existing one
        assertTrue(sprintService.isSprintOverlapping(1L, today, nextWeek.plusDays(2), null));

        // Sprint that is completely contained by an existing one
        assertTrue(sprintService.isSprintOverlapping(1L, tomorrow.plusDays(1), nextWeek, null));
    }

    @Test
    void isSprintOverlapping_WithNonOverlappingSprints_ShouldReturnFalse() {
        // Arrange
        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(tomorrow)
                .endDate(nextWeek)
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(List.of(existingSprint));

        // Act & Assert
        // Sprint that ends before an existing one starts
        assertFalse(sprintService.isSprintOverlapping(1L, today.minusDays(10), today.minusDays(1), null));

        // Sprint that starts after an existing one ends
        assertFalse(sprintService.isSprintOverlapping(1L, nextWeek.plusDays(1), nextWeek.plusDays(10), null));
    }

    @Test
    void isSprintOverlapping_ExcludingCurrentSprint_ShouldNotCheckCurrentSprint() {
        // Arrange
        Sprint sprint1 = Sprint.builder()
                .id(1L)
                .name("Sprint 1")
                .startDate(today)
                .endDate(nextWeek)
                .sprintBacklog(testSprintBacklog)
                .build();

        Sprint sprint2 = Sprint.builder()
                .id(2L)
                .name("Sprint 2")
                .startDate(nextWeek.plusDays(1))
                .endDate(nextWeek.plusDays(10))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Arrays.asList(sprint1, sprint2));

        // Act & Assert
        // Should return false because the only overlapping sprint is being excluded
        assertFalse(sprintService.isSprintOverlapping(1L, today, nextWeek, 1L));

        // Should return true because sprint2 is not excluded and would overlap
        assertTrue(sprintService.isSprintOverlapping(1L, today, nextWeek.plusDays(5), null));
    }
}