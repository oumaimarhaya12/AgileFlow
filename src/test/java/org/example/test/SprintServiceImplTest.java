package org.example.test;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.example.productbacklog.converter.SprintConverter;
import org.example.productbacklog.dto.SprintDTO;
import org.example.productbacklog.entity.Sprint;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.Statut;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.example.productbacklog.repository.SprintRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.service.impl.SprintServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SprintServiceImplTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private SprintBacklogRepository sprintBacklogRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Spy
    private SprintConverter sprintConverter;

    @InjectMocks
    private SprintServiceImpl sprintService;

    private Sprint testSprint;
    private SprintDTO testSprintDTO;
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

        testSprintDTO = SprintDTO.builder()
                .id(1L)
                .name("Test Sprint")
                .startDate(today)
                .endDate(nextWeek)
                .sprintBacklogId(1L)
                .build();

        lenient().doReturn(testSprintDTO).when(sprintConverter).convertToDTO(testSprint);
        lenient().doReturn(testSprint).when(sprintConverter).convertToEntity(testSprintDTO);

        List<SprintDTO> dtoList = Collections.singletonList(testSprintDTO);
        lenient().doReturn(dtoList).when(sprintConverter).convertToDTOList(any());
    }

    @Test
    void createSprint_WithSameDayStartAndEnd_ShouldCreateSprint() {
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.of(testSprintBacklog));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());

        SprintDTO result = sprintService.createSprint("One Day Sprint", today, today, 1L);

        assertNotNull(result);
        assertEquals("Test Sprint", result.getName());
        assertEquals(today, result.getStartDate());
        assertEquals(nextWeek, result.getEndDate());
        verify(sprintRepository).save(any(Sprint.class));
    }

    @Test
    void createSprint_WithMaximumDuration_ShouldCreateSprint() {
        LocalDate farFuture = today.plusYears(1);
        Sprint longSprint = Sprint.builder()
                .id(2L)
                .name("Long Sprint")
                .startDate(today)
                .endDate(farFuture)
                .sprintBacklog(testSprintBacklog)
                .build();

        SprintDTO longSprintDTO = SprintDTO.builder()
                .id(2L)
                .name("Long Sprint")
                .startDate(today)
                .endDate(farFuture)
                .sprintBacklogId(1L)
                .build();

        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.of(testSprintBacklog));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(longSprint);
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());
        when(sprintConverter.convertToDTO(longSprint)).thenReturn(longSprintDTO);

        SprintDTO result = sprintService.createSprint("Long Sprint", today, farFuture, 1L);

        assertNotNull(result);
        assertEquals("Long Sprint", result.getName());
        assertEquals(today, result.getStartDate());
        assertEquals(farFuture, result.getEndDate());
        verify(sprintRepository).save(any(Sprint.class));
    }

    @Test
    void createSprint_WithNullSprintBacklogId_ShouldThrowException() {
        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                sprintService.createSprint("Test Sprint", today, nextWeek, null)
        );
        assertTrue(exception.getMessage().contains("Sprint Backlog not found with ID: null"));
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void updateSprint_WithOnlyNameChange_ShouldUpdateOnlyName() {
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);

        SprintDTO result = sprintService.updateSprint(1L, "Updated Name", null, null);

        assertNotNull(result);
        verify(sprintRepository).save(testSprint);

        ArgumentCaptor<Sprint> sprintCaptor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(sprintCaptor.capture());
        Sprint capturedSprint = sprintCaptor.getValue();

        assertEquals("Updated Name", capturedSprint.getName());
        assertEquals(today, capturedSprint.getStartDate());
        assertEquals(nextWeek, capturedSprint.getEndDate());
    }

    @Test
    void updateSprint_WithOnlyStartDateChange_ShouldUpdateOnlyStartDate() {
        LocalDate newStartDate = today.plusDays(2);
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());

        SprintDTO result = sprintService.updateSprint(1L, null, newStartDate, null);

        assertNotNull(result);
        verify(sprintRepository).save(testSprint);

        ArgumentCaptor<Sprint> sprintCaptor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(sprintCaptor.capture());
        Sprint capturedSprint = sprintCaptor.getValue();

        assertEquals("Test Sprint", capturedSprint.getName());
        assertEquals(newStartDate, capturedSprint.getStartDate());
        assertEquals(nextWeek, capturedSprint.getEndDate());
    }

    @Test
    void updateSprint_WithOnlyEndDateChange_ShouldUpdateOnlyEndDate() {
        LocalDate newEndDate = nextWeek.plusDays(2);
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());

        SprintDTO result = sprintService.updateSprint(1L, null, null, newEndDate);

        assertNotNull(result);
        verify(sprintRepository).save(testSprint);

        ArgumentCaptor<Sprint> sprintCaptor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(sprintCaptor.capture());
        Sprint capturedSprint = sprintCaptor.getValue();

        assertEquals("Test Sprint", capturedSprint.getName());
        assertEquals(today, capturedSprint.getStartDate());
        assertEquals(newEndDate, capturedSprint.getEndDate());
    }

    @Test
    void updateSprint_WithInvalidStartDate_ShouldThrowException() {
        LocalDate invalidStartDate = nextWeek.plusDays(1);
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.updateSprint(1L, null, invalidStartDate, null)
        );
        assertEquals("Sprint end date must be after start date", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void updateSprint_WithInvalidEndDate_ShouldThrowException() {
        LocalDate invalidEndDate = today.minusDays(1);
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.updateSprint(1L, null, null, invalidEndDate)
        );
        assertEquals("Sprint end date must be after start date", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void updateSprint_WithOverlappingDates_ShouldThrowException() {
        LocalDate newStartDate = today.plusDays(2);
        LocalDate newEndDate = nextWeek.plusDays(2);

        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(newStartDate.plusDays(1))
                .endDate(newEndDate.plusDays(1))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));
        when(sprintRepository.findBySprintBacklogId(1L)).thenReturn(Arrays.asList(testSprint, existingSprint));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.updateSprint(1L, null, newStartDate, newEndDate)
        );
        assertEquals("Sprint dates overlap with an existing sprint in the same sprint backlog", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void getActiveSprintsByDate_WithSpecificDate_ShouldReturnActiveSprints() {
        LocalDate specificDate = today.plusDays(3);
        when(sprintRepository.findActiveSprintsByDate(specificDate)).thenReturn(List.of(testSprint));

        List<SprintDTO> result = sprintService.getActiveSprintsByDate(specificDate);

        assertEquals(1, result.size());
        assertEquals(testSprintDTO, result.get(0));
        verify(sprintRepository).findActiveSprintsByDate(specificDate);
    }

    @Test
    void getActiveSprintsByDate_WithNullDate_ShouldUseCurrentDate() {
        when(sprintRepository.findActiveSprintsByDate(any(LocalDate.class))).thenReturn(List.of(testSprint));

        List<SprintDTO> result = sprintService.getActiveSprintsByDate(null);

        assertEquals(1, result.size());
        assertEquals(testSprintDTO, result.get(0));

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(sprintRepository).findActiveSprintsByDate(dateCaptor.capture());
        LocalDate capturedDate = dateCaptor.getValue();

        assertEquals(LocalDate.now().getYear(), capturedDate.getYear());
        assertEquals(LocalDate.now().getMonth(), capturedDate.getMonth());
        assertEquals(LocalDate.now().getDayOfMonth(), capturedDate.getDayOfMonth());
    }

    @Test
    void getActiveSprintsByDate_WithNoActiveSprints_ShouldReturnEmptyList() {
        when(sprintRepository.findActiveSprintsByDate(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(sprintConverter.convertToDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<SprintDTO> result = sprintService.getActiveSprintsByDate(today);

        assertTrue(result.isEmpty());
    }

    @Test
    void getUpcomingSprints_WithMultipleUpcomingSprints_ShouldReturnAllUpcomingSprints() {
        Sprint upcomingSprint1 = Sprint.builder()
                .id(2L)
                .name("Upcoming Sprint 1")
                .startDate(nextWeek)
                .endDate(nextWeek.plusWeeks(1))
                .build();

        Sprint upcomingSprint2 = Sprint.builder()
                .id(3L)
                .name("Upcoming Sprint 2")
                .startDate(nextWeek.plusWeeks(1))
                .endDate(nextWeek.plusWeeks(2))
                .build();

        SprintDTO upcomingSprint1DTO = SprintDTO.builder()
                .id(2L)
                .name("Upcoming Sprint 1")
                .startDate(nextWeek)
                .endDate(nextWeek.plusWeeks(1))
                .build();

        SprintDTO upcomingSprint2DTO = SprintDTO.builder()
                .id(3L)
                .name("Upcoming Sprint 2")
                .startDate(nextWeek.plusWeeks(1))
                .endDate(nextWeek.plusWeeks(2))
                .build();

        List<Sprint> upcomingSprints = Arrays.asList(upcomingSprint1, upcomingSprint2);
        List<SprintDTO> upcomingSprintDTOs = Arrays.asList(upcomingSprint1DTO, upcomingSprint2DTO);

        when(sprintRepository.findUpcomingSprints(any(LocalDate.class))).thenReturn(upcomingSprints);
        when(sprintConverter.convertToDTOList(upcomingSprints)).thenReturn(upcomingSprintDTOs);

        List<SprintDTO> result = sprintService.getUpcomingSprints();

        assertEquals(2, result.size());
        assertEquals(upcomingSprint1DTO, result.get(0));
        assertEquals(upcomingSprint2DTO, result.get(1));
    }

    @Test
    void getCompletedSprints_WithMultipleCompletedSprints_ShouldReturnAllCompletedSprints() {
        Sprint completedSprint1 = Sprint.builder()
                .id(2L)
                .name("Completed Sprint 1")
                .startDate(today.minusWeeks(2))
                .endDate(today.minusWeeks(1))
                .build();

        Sprint completedSprint2 = Sprint.builder()
                .id(3L)
                .name("Completed Sprint 2")
                .startDate(today.minusWeeks(4))
                .endDate(today.minusWeeks(3))
                .build();

        SprintDTO completedSprint1DTO = SprintDTO.builder()
                .id(2L)
                .name("Completed Sprint 1")
                .startDate(today.minusWeeks(2))
                .endDate(today.minusWeeks(1))
                .build();

        SprintDTO completedSprint2DTO = SprintDTO.builder()
                .id(3L)
                .name("Completed Sprint 2")
                .startDate(today.minusWeeks(4))
                .endDate(today.minusWeeks(3))
                .build();

        List<Sprint> completedSprints = Arrays.asList(completedSprint1, completedSprint2);
        List<SprintDTO> completedSprintDTOs = Arrays.asList(completedSprint1DTO, completedSprint2DTO);

        when(sprintRepository.findCompletedSprints(any(LocalDate.class))).thenReturn(completedSprints);
        when(sprintConverter.convertToDTOList(completedSprints)).thenReturn(completedSprintDTOs);

        List<SprintDTO> result = sprintService.getCompletedSprints();

        assertEquals(2, result.size());
        assertEquals(completedSprint1DTO, result.get(0));
        assertEquals(completedSprint2DTO, result.get(1));
    }

    @Test
    void assignSprintToSprintBacklog_WithSprintAlreadyAssigned_ShouldReassign() {
        SprintBacklog oldSprintBacklog = new SprintBacklog();
        oldSprintBacklog.setId(2L);

        Sprint sprintWithBacklog = Sprint.builder()
                .id(1L)
                .name("Test Sprint")
                .startDate(today)
                .endDate(nextWeek)
                .sprintBacklog(oldSprintBacklog)
                .build();

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprintWithBacklog));
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(sprintRepository.findBySprintBacklogId(1L)).thenReturn(Collections.emptyList());
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);

        SprintDTO result = sprintService.assignSprintToSprintBacklog(1L, 1L);

        assertNotNull(result);
        assertEquals(testSprintDTO, result);

        ArgumentCaptor<Sprint> sprintCaptor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(sprintCaptor.capture());
        Sprint capturedSprint = sprintCaptor.getValue();

        assertEquals(testSprintBacklog, capturedSprint.getSprintBacklog());
    }

    @Test
    void assignSprintToSprintBacklog_WithOverlappingSprintsInTarget_ShouldThrowException() {
        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(today.minusDays(1))
                .endDate(nextWeek.plusDays(1))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprintBacklog));
        when(sprintRepository.findBySprintBacklogId(1L)).thenReturn(Arrays.asList(existingSprint));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.assignSprintToSprintBacklog(1L, 1L)
        );
        assertEquals("Sprint dates overlap with an existing sprint in the target sprint backlog", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void removeSprintFromSprintBacklog_WithNonExistentSprint_ShouldThrowException() {
        when(sprintRepository.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                sprintService.removeSprintFromSprintBacklog(999L)
        );
        assertEquals("Sprint not found with ID: 999", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void removeSprintFromSprintBacklog_WithNoSprintBacklog_ShouldReturnUnchanged() {
        Sprint sprintWithoutBacklog = Sprint.builder()
                .id(1L)
                .name("Test Sprint")
                .startDate(today)
                .endDate(nextWeek)
                .sprintBacklog(null)
                .build();

        SprintDTO sprintWithoutBacklogDTO = SprintDTO.builder()
                .id(1L)
                .name("Test Sprint")
                .startDate(today)
                .endDate(nextWeek)
                .sprintBacklogId(null)
                .build();

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprintWithoutBacklog));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(sprintWithoutBacklog);
        when(sprintConverter.convertToDTO(sprintWithoutBacklog)).thenReturn(sprintWithoutBacklogDTO);

        SprintDTO result = sprintService.removeSprintFromSprintBacklog(1L);

        assertNotNull(result);
        assertEquals(sprintWithoutBacklogDTO, result);
        verify(sprintRepository).save(sprintWithoutBacklog);
    }

    @Test
    void isSprintOverlapping_WithCompletelyContainedSprint_ShouldReturnTrue() {
        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(today.plusDays(2))
                .endDate(nextWeek.minusDays(2))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(1L)).thenReturn(List.of(existingSprint));

        boolean result = sprintService.isSprintOverlapping(1L, today, nextWeek, null);

        assertTrue(result);
    }

    @Test
    void isSprintOverlapping_WithCompletelyContainingSprint_ShouldReturnTrue() {
        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(today.minusDays(2))
                .endDate(nextWeek.plusDays(2))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(1L)).thenReturn(List.of(existingSprint));

        boolean result = sprintService.isSprintOverlapping(1L, today, nextWeek, null);

        assertTrue(result);
    }

    @Test
    void isSprintOverlapping_WithExactlySameDates_ShouldReturnTrue() {
        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(today)
                .endDate(nextWeek)
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(1L)).thenReturn(List.of(existingSprint));

        boolean result = sprintService.isSprintOverlapping(1L, today, nextWeek, null);

        assertTrue(result);
    }

    @Test
    void isSprintOverlapping_WithStartDateOverlap_ShouldReturnTrue() {
        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(today.minusDays(5))
                .endDate(today.plusDays(2))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(1L)).thenReturn(List.of(existingSprint));

        boolean result = sprintService.isSprintOverlapping(1L, today, nextWeek, null);

        assertTrue(result);
    }

    @Test
    void isSprintOverlapping_WithEndDateOverlap_ShouldReturnTrue() {
        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(nextWeek.minusDays(2))
                .endDate(nextWeek.plusDays(5))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(1L)).thenReturn(List.of(existingSprint));

        boolean result = sprintService.isSprintOverlapping(1L, today, nextWeek, null);

        assertTrue(result);
    }

    @Test
    void isSprintOverlapping_WithAdjacentSprints_ShouldReturnFalse() {
        Sprint existingSprint = Sprint.builder()
                .id(2L)
                .name("Existing Sprint")
                .startDate(nextWeek.plusDays(1))
                .endDate(nextWeek.plusDays(14))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(1L)).thenReturn(List.of(existingSprint));

        boolean result = sprintService.isSprintOverlapping(1L, today, nextWeek, null);

        assertFalse(result);
    }

    @Test
    void isSprintOverlapping_WithMultipleExistingSprints_ShouldCheckAll() {
        Sprint existingSprint1 = Sprint.builder()
                .id(2L)
                .name("Existing Sprint 1")
                .startDate(today.minusWeeks(2))
                .endDate(today.minusWeeks(1))
                .sprintBacklog(testSprintBacklog)
                .build();

        Sprint existingSprint2 = Sprint.builder()
                .id(3L)
                .name("Existing Sprint 2")
                .startDate(nextWeek.plusDays(1))
                .endDate(nextWeek.plusWeeks(1))
                .sprintBacklog(testSprintBacklog)
                .build();

        Sprint existingSprint3 = Sprint.builder()
                .id(4L)
                .name("Existing Sprint 3")
                .startDate(today.plusDays(2))
                .endDate(nextWeek.minusDays(2))
                .sprintBacklog(testSprintBacklog)
                .build();

        when(sprintRepository.findBySprintBacklogId(1L)).thenReturn(Arrays.asList(existingSprint1, existingSprint2, existingSprint3));

        boolean result = sprintService.isSprintOverlapping(1L, today, nextWeek, null);

        assertTrue(result);
    }

    @Test
    void createSprint_WithUserStories_ShouldCreateSprintWithUserStories() {
        UserStory userStory1 = UserStory.builder()
                .id(1L)
                .title("User Story 1")
                .status(Statut.toDo)
                .build();

        UserStory userStory2 = UserStory.builder()
                .id(2L)
                .title("User Story 2")
                .status(Statut.toDo)
                .build();

        testSprintBacklog.setUserStories(Arrays.asList(userStory1, userStory2));

        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.of(testSprintBacklog));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());

        SprintDTO result = sprintService.createSprint("Sprint with User Stories", today, nextWeek, 1L);

        assertNotNull(result);
        assertEquals("Test Sprint", result.getName());
        verify(sprintRepository).save(any(Sprint.class));
    }

    @Test
    void getAllSprints_WithEmptyRepository_ShouldReturnEmptyList() {
        when(sprintRepository.findAll()).thenReturn(Collections.emptyList());
        when(sprintConverter.convertToDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<SprintDTO> result = sprintService.getAllSprints();

        assertTrue(result.isEmpty());
    }

    @Test
    void getSprintsBySprintBacklogId_WithNoSprints_ShouldReturnEmptyList() {
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.of(testSprintBacklog));
        when(sprintRepository.findBySprintBacklogId(anyLong())).thenReturn(Collections.emptyList());
        when(sprintConverter.convertToDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<SprintDTO> result = sprintService.getSprintsBySprintBacklogId(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createSprint_WithNullName_ShouldThrowException() {
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.of(testSprintBacklog));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                sprintService.createSprint(null, today, nextWeek, 1L)
        );
        assertEquals("Sprint name cannot be empty", exception.getMessage());
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void updateSprint_WithEmptyName_ShouldNotUpdateName() {
        when(sprintRepository.findById(anyLong())).thenReturn(Optional.of(testSprint));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);

        SprintDTO result = sprintService.updateSprint(1L, "", null, null);

        assertNotNull(result);

        ArgumentCaptor<Sprint> sprintCaptor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(sprintCaptor.capture());
        Sprint capturedSprint = sprintCaptor.getValue();

        assertEquals("Test Sprint", capturedSprint.getName());
    }

    @Test
    void calculateSprintDuration_ShouldReturnCorrectDuration() {
        LocalDate startDate = LocalDate.of(2025, 4, 1);
        LocalDate endDate = LocalDate.of(2025, 4, 15);

        long duration = ChronoUnit.DAYS.between(startDate, endDate);

        assertEquals(14, duration);
    }

    @Test
    void isSprintActive_WithCurrentDateInRange_ShouldReturnTrue() {
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = currentDate.minusDays(1);
        LocalDate endDate = currentDate.plusDays(1);

        boolean isActive = (currentDate.isEqual(startDate) || currentDate.isAfter(startDate)) &&
                (currentDate.isEqual(endDate) || currentDate.isBefore(endDate));

        assertTrue(isActive);
    }

    @Test
    void isSprintActive_WithCurrentDateBeforeRange_ShouldReturnFalse() {
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = currentDate.plusDays(1);
        LocalDate endDate = currentDate.plusDays(10);

        boolean isActive = (currentDate.isEqual(startDate) || currentDate.isAfter(startDate)) &&
                (currentDate.isEqual(endDate) || currentDate.isBefore(endDate));

        assertFalse(isActive);
    }

    @Test
    void isSprintActive_WithCurrentDateAfterRange_ShouldReturnFalse() {
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = currentDate.minusDays(10);
        LocalDate endDate = currentDate.minusDays(1);

        boolean isActive = (currentDate.isEqual(startDate) || currentDate.isAfter(startDate)) &&
                (currentDate.isEqual(endDate) || currentDate.isBefore(endDate));

        assertFalse(isActive);
    }

    @Test
    void calculateSprintProgress_ShouldReturnCorrectPercentage() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        LocalDate currentDate = LocalDate.now();

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        long daysPassed = ChronoUnit.DAYS.between(startDate, currentDate);
        double progress = (double) daysPassed / totalDays * 100;

        assertEquals(50.0, progress, 0.1);
    }

    @Test
    void getSprintsByName_ShouldReturnMatchingSprints() {
        String sprintName = "Test Sprint";
        when(sprintRepository.findByName(sprintName)).thenReturn(testSprint);
        when(sprintConverter.convertToDTO(testSprint)).thenReturn(testSprintDTO);

        assertEquals(testSprint, sprintRepository.findByName(sprintName));
        assertEquals(testSprintDTO, sprintConverter.convertToDTO(testSprint));
    }
}