package org.example.productbacklog.converter;

import org.example.productbacklog.dto.TaskDTO;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.repository.UserRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskConverter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStoryRepository userStoryRepository;

    /**
     * Converts a Task entity to a TaskDTO
     * @param entity The Task entity to convert
     * @return The corresponding TaskDTO
     */
    public TaskDTO convertToDTO(Task entity) {
        if (entity == null) {
            return null;
        }

        TaskDTO dto = new TaskDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setDueDate(entity.getDueDate());
        dto.setPriority(entity.getPriority());
        dto.setEstimatedHours(entity.getEstimatedHours());
        dto.setLoggedHours(entity.getLoggedHours());

        if (entity.getUserStory() != null) {
            dto.setUserStoryId(entity.getUserStory().getId());
        }

        if (entity.getAssignedUser() != null) {
            dto.setAssignedUserId(entity.getAssignedUser().getId());
        }

        return dto;
    }

    /**
     * Converts a list of Task entities to a list of TaskDTOs
     * @param entities The list of Task entities to convert
     * @return The corresponding list of TaskDTOs
     */
    public List<TaskDTO> convertToDTOList(List<Task> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a TaskDTO to a Task entity
     * @param dto The TaskDTO to convert
     * @return The corresponding Task entity
     */
    public Task convertToEntity(TaskDTO dto) {
        if (dto == null) {
            return null;
        }

        Task entity = new Task();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            try {
                entity.setStatus(Task.TaskStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                // Default to TO_DO if status is invalid
                entity.setStatus(Task.TaskStatus.TO_DO);
            }
        }

        entity.setDueDate(dto.getDueDate());
        entity.setPriority(dto.getPriority());
        entity.setEstimatedHours(dto.getEstimatedHours());
        entity.setLoggedHours(dto.getLoggedHours());

        if (dto.getUserStoryId() != null) {
            userStoryRepository.findById(dto.getUserStoryId())
                    .ifPresent(entity::setUserStory);
        }

        if (dto.getAssignedUserId() != null) {
            userRepository.findById(dto.getAssignedUserId())
                    .ifPresent(entity::setAssignedUser);
        }

        return entity;
    }

    /**
     * Updates an existing Task entity with data from a TaskDTO
     * @param entity The Task entity to update
     * @param dto The TaskDTO containing the new data
     * @return The updated Task entity
     */
    public Task updateEntityFromDTO(Task entity, TaskDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }

        if (dto.getStatus() != null) {
            try {
                entity.setStatus(Task.TaskStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        if (dto.getDueDate() != null) {
            entity.setDueDate(dto.getDueDate());
        }

        if (dto.getPriority() > 0) {
            entity.setPriority(dto.getPriority());
        }

        if (dto.getEstimatedHours() > 0) {
            entity.setEstimatedHours(dto.getEstimatedHours());
        }

        if (dto.getLoggedHours() > 0) {
            entity.setLoggedHours(dto.getLoggedHours());
        }

        if (dto.getUserStoryId() != null) {
            userStoryRepository.findById(dto.getUserStoryId())
                    .ifPresent(entity::setUserStory);
        }

        if (dto.getAssignedUserId() != null) {
            userRepository.findById(dto.getAssignedUserId())
                    .ifPresent(entity::setAssignedUser);
        }

        return entity;
    }
}