package org.example.productbacklog.service;

import org.example.productbacklog.dto.UserStoryDTO;
import org.example.productbacklog.entity.Statut;

import java.util.List;
import java.util.Optional;

public interface UserStoryService {

    // CRUD operations
    UserStoryDTO saveUserStory(UserStoryDTO userStoryDTO);
    Optional<UserStoryDTO> getUserStoryById(Long id);
    List<UserStoryDTO> getAllUserStories();
    void deleteUserStory(Long id);

    // Epic-related operations
    UserStoryDTO linkUserStoryToEpic(Long userStoryId, Integer epicId);
    List<UserStoryDTO> getUserStoriesByEpic(Integer epicId);
    List<UserStoryDTO> getUserStoriesWithoutEpic();

    // Sprint Backlog operations
    UserStoryDTO addUserStoryToSprintBacklog(Long userStoryId, Long sprintBacklogId);
    List<UserStoryDTO> getUserStoriesBySprintBacklog(Long sprintBacklogId);

    // Product Backlog operations
    List<UserStoryDTO> getUserStoriesByProductBacklog(Long productBacklogId);
    List<UserStoryDTO> getPrioritizedUserStories(Long productBacklogId);
    UserStoryDTO updateUserStoryPriority(Long userStoryId, int newPriority);

    // Acceptance criteria operations
    UserStoryDTO updateAcceptanceCriteria(Long userStoryId, String acceptanceCriteria);
    List<UserStoryDTO> getUserStoriesWithoutAcceptanceCriteria();
}