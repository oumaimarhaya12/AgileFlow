package org.example.productbacklog.repository;

import org.example.productbacklog.entity.Epic;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.entity.UserStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

    // Find User Stories by Epic
    List<UserStory> findByEpic(Epic epic);

    // Find User Stories by Epic ID
    List<UserStory> findByEpicId(Integer epicId);

    // Find User Stories by Product Backlog
    List<UserStory> findByProductBacklog(ProductBacklog productBacklog);

    // Find User Stories by Product Backlog ID
    List<UserStory> findByProductBacklogId(Long productBacklogId);

    // Find User Stories by Sprint Backlog
    List<UserStory> findBySprintBacklog(SprintBacklog sprintBacklog);

    // Find User Stories by Sprint Backlog ID
    List<UserStory> findBySprintBacklogId(Long sprintBacklogId);

    // Find User Stories by Product Backlog ID ordered by priority
    List<UserStory> findByProductBacklogIdOrderByPriorityDesc(Long productBacklogId);

    // Find User Stories not assigned to any Sprint Backlog
    List<UserStory> findBySprintBacklogIsNull();

    // Find User Stories not assigned to any Epic
    List<UserStory> findByEpicIsNull();

    // Find User Stories with missing acceptance criteria
    @Query("SELECT us FROM UserStory us WHERE us.acceptanceCriteria IS NULL OR us.acceptanceCriteria = ''")
    List<UserStory> findWithoutAcceptanceCriteria();
}