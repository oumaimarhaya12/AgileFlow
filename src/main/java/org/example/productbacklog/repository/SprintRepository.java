package org.example.productbacklog.repository;

import org.example.productbacklog.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    // Find sprints by sprint backlog ID
    List<Sprint> findBySprintBacklogId(Long sprintBacklogId);

    // Find current active sprint (where current date is between start and end date)
    @Query("SELECT s FROM Sprint s WHERE :currentDate BETWEEN s.startDate AND s.endDate")
    List<Sprint> findActiveSprintsByDate(LocalDate currentDate);

    // Find sprints by name
    Sprint findByName(String name);

    // Find upcoming sprints (where start date is after current date)
    @Query("SELECT s FROM Sprint s WHERE s.startDate > :currentDate ORDER BY s.startDate ASC")
    List<Sprint> findUpcomingSprints(LocalDate currentDate);

    // Find completed sprints (where end date is before current date)
    @Query("SELECT s FROM Sprint s WHERE s.endDate < :currentDate ORDER BY s.endDate DESC")
    List<Sprint> findCompletedSprints(LocalDate currentDate);
}