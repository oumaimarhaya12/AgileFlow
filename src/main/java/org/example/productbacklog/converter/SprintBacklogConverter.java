package org.example.productbacklog.converter;

import org.example.productbacklog.dto.SprintBacklogDTO;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.entity.Sprint;
import org.example.productbacklog.entity.UserStory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SprintBacklogConverter {

    /**
     * Convert a SprintBacklog entity to a SprintBacklogDTO
     */
    public SprintBacklogDTO convertToDTO(SprintBacklog entity) {
        if (entity == null) {
            return null;
        }

        SprintBacklogDTO.SprintBacklogDTOBuilder builder = SprintBacklogDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle());

        // Handle the ProductBacklog ID conversion - convert from Integer to Integer
        if (entity.getProductBacklog() != null) {
            builder.productBacklogId(entity.getProductBacklog().getId());
        }

        // Extract UserStory IDs if available
        if (entity.getUserStories() != null) {
            List<Long> userStoryIds = entity.getUserStories().stream()
                    .map(UserStory::getId)
                    .collect(Collectors.toList());
            builder.userStoryIds(userStoryIds);
        }

        // Extract Sprint IDs if available
        if (entity.getSprints() != null) {
            List<Long> sprintIds = entity.getSprints().stream()
                    .map(Sprint::getId)
                    .collect(Collectors.toList());
            builder.sprintIds(sprintIds);
        }

        return builder.build();
    }

    /**
     * Convert a SprintBacklogDTO to a SprintBacklog entity
     * Note: This doesn't set the references to ProductBacklog, UserStories, or Sprints,
     * which should be handled by the service
     */
    public SprintBacklog convertToEntity(SprintBacklogDTO dto) {
        if (dto == null) {
            return null;
        }

        SprintBacklog entity = new SprintBacklog();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());

        // Don't set ProductBacklog, UserStories, or Sprints here
        // These should be handled by the service that has access to the repositories

        return entity;
    }

    /**
     * Convert a list of SprintBacklog entities to a list of SprintBacklogDTOs
     */
    public List<SprintBacklogDTO> convertToDTOList(List<SprintBacklog> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert a list of SprintBacklogDTOs to a list of SprintBacklog entities
     */
    public List<SprintBacklog> convertToEntityList(List<SprintBacklogDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        return dtos.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
}