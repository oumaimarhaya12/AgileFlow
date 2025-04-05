package org.example.productbacklog.converter;

import org.example.productbacklog.dto.UserStoryDTO;
import org.example.productbacklog.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserStoryConverter {

    /**
     * Converts a UserStory entity to a UserStoryDTO
     * @param entity The UserStory entity to convert
     * @return The corresponding UserStoryDTO
     */
    public UserStoryDTO convertToDTO(UserStory entity) {
        if (entity == null) {
            return null;
        }

        UserStoryDTO dto = new UserStoryDTO();
        dto.setTitle(entity.getTitle());
        dto.setAsA(entity.getAsA());
        dto.setIWant(entity.getIWant());
        dto.setSoThat(entity.getSoThat());
        dto.setDescription(entity.getDescription());
        dto.setAcceptanceCriteria(entity.getAcceptanceCriteria());
        dto.setPriority(entity.getPriority());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);

        // Set related entity IDs
        if (entity.getEpic() != null) {
            dto.setEpicId(entity.getEpic().getId().intValue());
        }

        if (entity.getProductBacklog() != null) {
            dto.setProductBacklogId(entity.getProductBacklog().getId());
        }

        if (entity.getSprintBacklog() != null) {
            dto.setSprintBacklogId(entity.getSprintBacklog().getId());
        }

        return dto;
    }

    /**
     * Converts a list of UserStory entities to a list of UserStoryDTOs
     * @param entities The list of UserStory entities to convert
     * @return The corresponding list of UserStoryDTOs
     */
    public List<UserStoryDTO> convertToDTOList(List<UserStory> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a UserStoryDTO to a UserStory entity
     * Note: This method does not set relationships (epic, productBacklog, sprintBacklog)
     * as these are typically managed through service methods
     * @param dto The UserStoryDTO to convert
     * @return The corresponding UserStory entity
     */
    public UserStory convertToEntity(UserStoryDTO dto) {
        if (dto == null) {
            return null;
        }

        return UserStory.builder()
                .title(dto.getTitle())
                .asA(dto.getAsA())
                .iWant(dto.getIWant())
                .soThat(dto.getSoThat())
                .description(dto.getDescription())
                .acceptanceCriteria(dto.getAcceptanceCriteria())
                .priority(dto.getPriority())
                .status(dto.getStatus() != null ? Statut.valueOf(dto.getStatus()) : null)
                .build();
    }

    /**
     * Updates an existing UserStory entity with data from a UserStoryDTO
     * Note: This method does not update relationships (epic, productBacklog, sprintBacklog)
     * as these are typically managed through service methods
     * @param entity The UserStory entity to update
     * @param dto The UserStoryDTO containing the new data
     * @return The updated UserStory entity
     */
    public UserStory updateEntityFromDTO(UserStory entity, UserStoryDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }

        if (dto.getAsA() != null) {
            entity.setAsA(dto.getAsA());
        }

        if (dto.getIWant() != null) {
            entity.setIWant(dto.getIWant());
        }

        if (dto.getSoThat() != null) {
            entity.setSoThat(dto.getSoThat());
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }

        if (dto.getAcceptanceCriteria() != null) {
            entity.setAcceptanceCriteria(dto.getAcceptanceCriteria());
        }

        if (dto.getPriority() > 0) {
            entity.setPriority(dto.getPriority());
        }

        if (dto.getStatus() != null) {
            entity.setStatus(Statut.valueOf(dto.getStatus()));
        }

        return entity;
    }
}