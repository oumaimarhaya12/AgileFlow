package org.example.productbacklog.service.impl;

import org.example.productbacklog.converter.UserStoryConverter;
import org.example.productbacklog.dto.UserStoryDTO;
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
    private final UserStoryConverter userStoryConverter;

    @Autowired
    public UserStoryServiceImpl(
            UserStoryRepository userStoryRepository,
            EpicRepository epicRepository,
            SprintBacklogRepository sprintBacklogRepository,
            ProductBacklogRepository productBacklogRepository,
            UserStoryConverter userStoryConverter) {
        this.userStoryRepository = userStoryRepository;
        this.epicRepository = epicRepository;
        this.sprintBacklogRepository = sprintBacklogRepository;
        this.productBacklogRepository = productBacklogRepository;
        this.userStoryConverter = userStoryConverter;
    }

    // CRUD operations
    @Override
    public UserStoryDTO saveUserStory(UserStoryDTO userStoryDTO) {
        UserStory userStory = userStoryConverter.convertToEntity(userStoryDTO);

        // Set relationships if IDs are provided
        if (userStoryDTO.getEpicId() != null) {
            epicRepository.findById(userStoryDTO.getEpicId().longValue())
                    .ifPresent(userStory::setEpic);
        }

        if (userStoryDTO.getProductBacklogId() != null) {
            productBacklogRepository.findById(userStoryDTO.getProductBacklogId())
                    .ifPresent(userStory::setProductBacklog);
        }

        if (userStoryDTO.getSprintBacklogId() != null) {
            sprintBacklogRepository.findById(userStoryDTO.getSprintBacklogId())
                    .ifPresent(userStory::setSprintBacklog);
        }

        UserStory savedUserStory = userStoryRepository.save(userStory);
        return userStoryConverter.convertToDTO(savedUserStory);
    }

    @Override
    public Optional<UserStoryDTO> getUserStoryById(Long id) {
        return userStoryRepository.findById(id)
                .map(userStoryConverter::convertToDTO);
    }

    @Override
    public List<UserStoryDTO> getAllUserStories() {
        List<UserStory> userStories = userStoryRepository.findAll();
        return userStoryConverter.convertToDTOList(userStories);
    }

    @Override
    public void deleteUserStory(Long id) {
        userStoryRepository.deleteById(id);
    }

    // Epic-related operations
    @Override
    @Transactional
    public UserStoryDTO linkUserStoryToEpic(Long userStoryId, Integer epicId) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User Story not found with id: " + userStoryId));

        Epic epic = epicRepository.findById(Long.valueOf(epicId))
                .orElseThrow(() -> new RuntimeException("Epic not found with id: " + epicId));

        userStory.setEpic(epic);
        UserStory updatedUserStory = userStoryRepository.save(userStory);
        return userStoryConverter.convertToDTO(updatedUserStory);
    }

    @Override
    public List<UserStoryDTO> getUserStoriesByEpic(Integer epicId) {
        List<UserStory> userStories = userStoryRepository.findByEpicId(epicId);
        return userStoryConverter.convertToDTOList(userStories);
    }

    @Override
    public List<UserStoryDTO> getUserStoriesWithoutEpic() {
        List<UserStory> userStories = userStoryRepository.findByEpicIsNull();
        return userStoryConverter.convertToDTOList(userStories);
    }

    // Sprint Backlog operations
    @Override
    @Transactional
    public UserStoryDTO addUserStoryToSprintBacklog(Long userStoryId, Long sprintBacklogId) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User Story not found with id: " + userStoryId));

        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new RuntimeException("Sprint Backlog not found with id: " + sprintBacklogId));

        userStory.setSprintBacklog(sprintBacklog);
        UserStory updatedUserStory = userStoryRepository.save(userStory);
        return userStoryConverter.convertToDTO(updatedUserStory);
    }

    @Override
    public List<UserStoryDTO> getUserStoriesBySprintBacklog(Long sprintBacklogId) {
        List<UserStory> userStories = userStoryRepository.findBySprintBacklogId(sprintBacklogId);
        return userStoryConverter.convertToDTOList(userStories);
    }

    // Product Backlog operations
    @Override
    public List<UserStoryDTO> getUserStoriesByProductBacklog(Long productBacklogId) {
        List<UserStory> userStories = userStoryRepository.findByProductBacklogId(productBacklogId);
        return userStoryConverter.convertToDTOList(userStories);
    }

    @Override
    public List<UserStoryDTO> getPrioritizedUserStories(Long productBacklogId) {
        List<UserStory> userStories = userStoryRepository.findByProductBacklogIdOrderByPriorityDesc(productBacklogId);
        return userStoryConverter.convertToDTOList(userStories);
    }

    @Override
    @Transactional
    public UserStoryDTO updateUserStoryPriority(Long userStoryId, int newPriority) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User Story not found with id: " + userStoryId));

        userStory.setPriority(newPriority);
        UserStory updatedUserStory = userStoryRepository.save(userStory);
        return userStoryConverter.convertToDTO(updatedUserStory);
    }

    // Acceptance criteria operations
    @Override
    @Transactional
    public UserStoryDTO updateAcceptanceCriteria(Long userStoryId, String acceptanceCriteria) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User Story not found with id: " + userStoryId));

        userStory.setAcceptanceCriteria(acceptanceCriteria);
        UserStory updatedUserStory = userStoryRepository.save(userStory);
        return userStoryConverter.convertToDTO(updatedUserStory);
    }

    @Override
    public List<UserStoryDTO> getUserStoriesWithoutAcceptanceCriteria() {
        List<UserStory> userStories = userStoryRepository.findWithoutAcceptanceCriteria();
        return userStoryConverter.convertToDTOList(userStories);
    }
}