package org.example.productbacklog.service.impl;

import org.example.productbacklog.entity.Epic;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.repository.EpicRepository;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.service.UserStoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserStoryServiceImpl implements UserStoryService {

    private final UserStoryRepository userStoryRepository;
    private final EpicRepository epicRepository;
    private final SprintBacklogRepository sprintBacklogRepository;
    private final ProductBacklogRepository productBacklogRepository;

    @Autowired
    public UserStoryServiceImpl(
            UserStoryRepository userStoryRepository,
            EpicRepository epicRepository,
            SprintBacklogRepository sprintBacklogRepository,
            ProductBacklogRepository productBacklogRepository) {
        this.userStoryRepository = userStoryRepository;
        this.epicRepository = epicRepository;
        this.sprintBacklogRepository = sprintBacklogRepository;
        this.productBacklogRepository = productBacklogRepository;
    }

    // CRUD operations
    @Override
    public UserStory saveUserStory(UserStory userStory) {
        return userStoryRepository.save(userStory);
    }

    @Override
    public Optional<UserStory> getUserStoryById(Long id) {
        return userStoryRepository.findById(id);
    }

    @Override
    public List<UserStory> getAllUserStories() {
        return userStoryRepository.findAll();
    }

    @Override
    public void deleteUserStory(Long id) {
        userStoryRepository.deleteById(id);
    }

    // Epic-related operations
    @Override
    @Transactional
    public UserStory linkUserStoryToEpic(Long userStoryId, Integer epicId) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User Story not found with id: " + userStoryId));

        Epic epic = epicRepository.findById(Long.valueOf(epicId))
                .orElseThrow(() -> new RuntimeException("Epic not found with id: " + epicId));

        userStory.setEpic(epic);
        return userStoryRepository.save(userStory);
    }

    @Override
    public List<UserStory> getUserStoriesByEpic(Integer epicId) {
        return userStoryRepository.findByEpicId(epicId);
    }

    @Override
    public List<UserStory> getUserStoriesWithoutEpic() {
        return userStoryRepository.findByEpicIsNull();
    }

    // Sprint Backlog operations
    @Override
    @Transactional
    public UserStory addUserStoryToSprintBacklog(Long userStoryId, Long sprintBacklogId) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User Story not found with id: " + userStoryId));

        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new RuntimeException("Sprint Backlog not found with id: " + sprintBacklogId));

        userStory.setSprintBacklog(sprintBacklog);
        return userStoryRepository.save(userStory);
    }

    @Override
    public List<UserStory> getUserStoriesBySprintBacklog(Long sprintBacklogId) {
        return userStoryRepository.findBySprintBacklogId(sprintBacklogId);
    }

    // Product Backlog operations
    @Override
    public List<UserStory> getUserStoriesByProductBacklog(Long productBacklogId) {
        return userStoryRepository.findByProductBacklogId(productBacklogId);
    }

    @Override
    public List<UserStory> getPrioritizedUserStories(Long productBacklogId) {
        return userStoryRepository.findByProductBacklogIdOrderByPriorityDesc(productBacklogId);
    }

    @Override
    @Transactional
    public UserStory updateUserStoryPriority(Long userStoryId, int newPriority) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User Story not found with id: " + userStoryId));

        userStory.setPriority(newPriority);
        return userStoryRepository.save(userStory);
    }

    // Acceptance criteria operations
    @Override
    @Transactional
    public UserStory updateAcceptanceCriteria(Long userStoryId, String acceptanceCriteria) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User Story not found with id: " + userStoryId));

        userStory.setAcceptanceCriteria(acceptanceCriteria);
        return userStoryRepository.save(userStory);
    }

    @Override
    public List<UserStory> getUserStoriesWithoutAcceptanceCriteria() {
        return userStoryRepository.findWithoutAcceptanceCriteria();
    }
}