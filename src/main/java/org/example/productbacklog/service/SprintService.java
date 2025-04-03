package org.example.productbacklog.service;

import org.example.productbacklog.entity.Sprint;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SprintService {

    // Basic CRUD operations
    Sprint createSprint(String name, LocalDate startDate, LocalDate endDate, Long sprintBacklogId);

    Optional<Sprint> getSprintById(Long id);

    List<Sprint> getAllSprints();

    Sprint updateSprint(Long id, String name, LocalDate startDate, LocalDate endDate);

    void deleteSprint(Long id);

    // Specific operations
    List<Sprint> getSprintsBySprintBacklogId(Long sprintBacklogId);

    List<Sprint> getActiveSprintsByDate(LocalDate date);

    List<Sprint> getUpcomingSprints();

    List<Sprint> getCompletedSprints();

    // Sprint management operations
    Sprint assignSprintToSprintBacklog(Long sprintId, Long sprintBacklogId);

    Sprint removeSprintFromSprintBacklog(Long sprintId);

    // Duration validation and overlap checking
    boolean isSprintDateRangeValid(LocalDate startDate, LocalDate endDate);

    boolean isSprintOverlapping(Long sprintBacklogId, LocalDate startDate, LocalDate endDate, Long excludeSprintId);
}