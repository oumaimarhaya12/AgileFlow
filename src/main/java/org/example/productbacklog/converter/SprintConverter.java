package org.example.productbacklog.converter;

import org.example.productbacklog.dto.SprintDTO;
import org.example.productbacklog.entity.Sprint;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SprintConverter {

    @Autowired
    private SprintBacklogRepository sprintBacklogRepository;

    public SprintDTO convertToDTO(Sprint entity) {
        if (entity == null) {
            return null;
        }

        return SprintDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .sprintBacklogId(entity.getSprintBacklog() != null ? entity.getSprintBacklog().getId() : null)
                .build();
    }

    public List<SprintDTO> convertToDTOList(List<Sprint> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Sprint convertToEntity(SprintDTO dto) {
        if (dto == null) {
            return null;
        }

        Sprint entity = new Sprint();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());

        // Set sprint backlog if sprintBacklogId is provided
        if (dto.getSprintBacklogId() != null) {
            sprintBacklogRepository.findById(dto.getSprintBacklogId())
                    .ifPresent(entity::setSprintBacklog);
        }

        return entity;
    }

    public Sprint updateEntityFromDTO(Sprint entity, SprintDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }

        if (dto.getEndDate() != null) {
            entity.setEndDate(dto.getEndDate());
        }

        if (dto.getSprintBacklogId() != null) {
            sprintBacklogRepository.findById(dto.getSprintBacklogId())
                    .ifPresent(entity::setSprintBacklog);
        }

        return entity;
    }
}