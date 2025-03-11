package org.example.productbacklog.service;

import org.example.productbacklog.entity.Epic;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.entity.UserStory;

import java.util.List;
import java.util.Optional;

public interface UserStoryService {

    // CRUD operations
    UserStory saveUserStory(UserStory userStory);
    Optional<UserStory> getUserStoryById(Long id);
    List<UserStory> getAllUserStories();
    void deleteUserStory(Long id);

    // Epic-related operations
    UserStory linkUserStoryToEpic(Long userStoryId, Integer epicId);
    List<UserStory> getUserStoriesByEpic(Integer epicId);
    List<UserStory> getUserStoriesWithoutEpic();

    // Sprint Backlog operations
    UserStory addUserStoryToSprintBacklog(Long userStoryId, Long sprintBacklogId);
    List<UserStory> getUserStoriesBySprintBacklog(Long sprintBacklogId);

    // Product Backlog operations
    List<UserStory> getUserStoriesByProductBacklog(Long productBacklogId);
    List<UserStory> getPrioritizedUserStories(Long productBacklogId);
    UserStory updateUserStoryPriority(Long userStoryId, int newPriority);

    // Acceptance criteria operations
    UserStory updateAcceptanceCriteria(Long userStoryId, String acceptanceCriteria);
    List<UserStory> getUserStoriesWithoutAcceptanceCriteria();
}