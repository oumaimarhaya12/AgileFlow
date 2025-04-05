package org.example.productbacklog.service;

import org.example.productbacklog.dto.SprintDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SprintService {

    // Basic CRUD operations
    SprintDTO createSprint(String name, LocalDate startDate, LocalDate endDate, Long sprintBacklogId);

    Optional<SprintDTO> getSprintById(Long id);

    List<SprintDTO> getAllSprints();

    SprintDTO updateSprint(Long id, String name, LocalDate startDate, LocalDate endDate);

    void deleteSprint(Long id);

    // Specific operations
    List<SprintDTO> getSprintsBySprintBacklogId(Long sprintBacklogId);

    List<SprintDTO> getActiveSprintsByDate(LocalDate date);

    List<SprintDTO> getUpcomingSprints();

    List<SprintDTO> getCompletedSprints();

    // Sprint management operations
    SprintDTO assignSprintToSprintBacklog(Long sprintId, Long sprintBacklogId);

    SprintDTO removeSprintFromSprintBacklog(Long sprintId);

    // Duration validation and overlap checking
    boolean isSprintDateRangeValid(LocalDate startDate, LocalDate endDate);

    boolean isSprintOverlapping(Long sprintBacklogId, LocalDate startDate, LocalDate endDate, Long excludeSprintId);
}