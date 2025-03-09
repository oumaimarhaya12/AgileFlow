package org.example.productbacklog.repository;

import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.entity.UserStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserStory(UserStory userStory);

    List<Task> findByAssignedUser(User user);

    List<Task> findByStatus(Task.TaskStatus status);

    List<Task> findByDueDateBefore(LocalDateTime date);

    List<Task> findByDueDateAfter(LocalDateTime date);

    @Query("SELECT t FROM Task t WHERE t.userStory.id = :userStoryId")
    List<Task> findByUserStoryId(@Param("userStoryId") Long userStoryId);

    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId")
    List<Task> findByAssignedUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.userStory.id = :userStoryId AND t.status = :status")
    List<Task> findByUserStoryIdAndStatus(@Param("userStoryId") Long userStoryId, @Param("status") Task.TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.userStory.id = :userStoryId")
    Long countTasksByUserStoryId(@Param("userStoryId") Long userStoryId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.userStory.id = :userStoryId AND t.status = :status")
    Long countTasksByUserStoryIdAndStatus(@Param("userStoryId") Long userStoryId, @Param("status") Task.TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.status IN :statuses ORDER BY t.priority DESC")
    List<Task> findByStatusInOrderByPriorityDesc(@Param("statuses") List<Task.TaskStatus> statuses);

    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.dueDate BETWEEN :startDate AND :endDate")
    List<Task> findByAssignedUserIdAndDueDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(t.loggedHours) FROM Task t WHERE t.userStory.id = :userStoryId")
    Double getAverageLoggedHoursByUserStoryId(@Param("userStoryId") Long userStoryId);
}