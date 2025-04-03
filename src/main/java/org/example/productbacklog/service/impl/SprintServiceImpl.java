package org.example.productbacklog.service.impl;

import org.example.productbacklog.entity.Sprint;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.repository.SprintRepository;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.example.productbacklog.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final SprintBacklogRepository sprintBacklogRepository;

    @Autowired
    public SprintServiceImpl(
            SprintRepository sprintRepository,
            SprintBacklogRepository sprintBacklogRepository) {
        this.sprintRepository = sprintRepository;
        this.sprintBacklogRepository = sprintBacklogRepository;
    }

    @Override
    @Transactional
    public Sprint createSprint(String name, LocalDate startDate, LocalDate endDate, Long sprintBacklogId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Sprint name cannot be empty");
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Sprint start date and end date cannot be null");
        }

        if (!isSprintDateRangeValid(startDate, endDate)) {
            throw new IllegalArgumentException("Sprint end date must be after start date");
        }

        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog not found with ID: " + sprintBacklogId));

        // Check for overlapping sprints in the same sprint backlog
        if (isSprintOverlapping(sprintBacklogId, startDate, endDate, null)) {
            throw new IllegalArgumentException("Sprint dates overlap with an existing sprint in the same sprint backlog");
        }

        Sprint sprint = Sprint.builder()
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .sprintBacklog(sprintBacklog)
                .build();

        return sprintRepository.save(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sprint> getSprintById(Long id) {
        return sprintRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sprint> getAllSprints() {
        return sprintRepository.findAll();
    }

    @Override
    @Transactional
    public Sprint updateSprint(Long id, String name, LocalDate startDate, LocalDate endDate) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found with ID: " + id));

        if (name != null && !name.trim().isEmpty()) {
            sprint.setName(name);
        }

        if (startDate != null && endDate != null) {
            if (!isSprintDateRangeValid(startDate, endDate)) {
                throw new IllegalArgumentException("Sprint end date must be after start date");
            }

            // Check for overlapping sprints in the same sprint backlog
            if (sprint.getSprintBacklog() != null &&
                    isSprintOverlapping(sprint.getSprintBacklog().getId(), startDate, endDate, id)) {
                throw new IllegalArgumentException("Sprint dates overlap with an existing sprint in the same sprint backlog");
            }

            sprint.setStartDate(startDate);
            sprint.setEndDate(endDate);
        } else if (startDate != null) {
            // Only start date is updated
            if (!isSprintDateRangeValid(startDate, sprint.getEndDate())) {
                throw new IllegalArgumentException("Sprint end date must be after start date");
            }

            // Check for overlapping sprints
            if (sprint.getSprintBacklog() != null &&
                    isSprintOverlapping(sprint.getSprintBacklog().getId(), startDate, sprint.getEndDate(), id)) {
                throw new IllegalArgumentException("Sprint dates overlap with an existing sprint in the same sprint backlog");
            }

            sprint.setStartDate(startDate);
        } else if (endDate != null) {
            // Only end date is updated
            if (!isSprintDateRangeValid(sprint.getStartDate(), endDate)) {
                throw new IllegalArgumentException("Sprint end date must be after start date");
            }

            // Check for overlapping sprints
            if (sprint.getSprintBacklog() != null &&
                    isSprintOverlapping(sprint.getSprintBacklog().getId(), sprint.getStartDate(), endDate, id)) {
                throw new IllegalArgumentException("Sprint dates overlap with an existing sprint in the same sprint backlog");
            }

            sprint.setEndDate(endDate);
        }

        return sprintRepository.save(sprint);
    }

    @Override
    @Transactional
    public void deleteSprint(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found with ID: " + id));

        sprintRepository.delete(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sprint> getSprintsBySprintBacklogId(Long sprintBacklogId) {
        sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog not found with ID: " + sprintBacklogId));

        return sprintRepository.findBySprintBacklogId(sprintBacklogId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sprint> getActiveSprintsByDate(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return sprintRepository.findActiveSprintsByDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sprint> getUpcomingSprints() {
        return sprintRepository.findUpcomingSprints(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sprint> getCompletedSprints() {
        return sprintRepository.findCompletedSprints(LocalDate.now());
    }

    @Override
    @Transactional
    public Sprint assignSprintToSprintBacklog(Long sprintId, Long sprintBacklogId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found with ID: " + sprintId));

        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog not found with ID: " + sprintBacklogId));

        // Check for overlapping sprints in the target sprint backlog
        if (isSprintOverlapping(sprintBacklogId, sprint.getStartDate(), sprint.getEndDate(), sprintId)) {
            throw new IllegalArgumentException("Sprint dates overlap with an existing sprint in the target sprint backlog");
        }

        sprint.setSprintBacklog(sprintBacklog);
        return sprintRepository.save(sprint);
    }

    @Override
    @Transactional
    public Sprint removeSprintFromSprintBacklog(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found with ID: " + sprintId));

        sprint.setSprintBacklog(null);
        return sprintRepository.save(sprint);
    }

    @Override
    public boolean isSprintDateRangeValid(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !endDate.isBefore(startDate);
    }

    @Override
    public boolean isSprintOverlapping(Long sprintBacklogId, LocalDate startDate, LocalDate endDate, Long excludeSprintId) {
        if (sprintBacklogId == null || startDate == null || endDate == null) {
            return false;
        }

        List<Sprint> existingSprints = sprintRepository.findBySprintBacklogId(sprintBacklogId);

        for (Sprint existingSprint : existingSprints) {
            // Skip the sprint being updated (if excludeSprintId is provided)
            if (excludeSprintId != null && existingSprint.getId().equals(excludeSprintId)) {
                continue;
            }

            // Check if there's an overlap:
            // New sprint starts during an existing one OR
            // New sprint ends during an existing one OR
            // New sprint entirely contains an existing one
            boolean overlaps = (startDate.isBefore(existingSprint.getEndDate()) || startDate.isEqual(existingSprint.getEndDate())) &&
                    (endDate.isAfter(existingSprint.getStartDate()) || endDate.isEqual(existingSprint.getStartDate()));

            if (overlaps) {
                return true;
            }
        }

        return false;
    }
}