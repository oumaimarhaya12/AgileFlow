package org.example.productbacklog.converter;

import org.example.productbacklog.dto.ProductBacklogDTO;
import org.example.productbacklog.entity.Epic;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.repository.EpicRepository;
import org.example.productbacklog.repository.ProjectRepository;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductBacklogConverter {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EpicRepository epicRepository;

    @Autowired
    private SprintBacklogRepository sprintBacklogRepository;

    public ProductBacklogDTO entityToDto(ProductBacklog entity) {
        if (entity == null) {
            return null;
        }

        ProductBacklogDTO dto = new ProductBacklogDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());

        // Set project ID if available
        if (entity.getProject() != null) {
            dto.setProjectId(entity.getProject().getProjectId());
        }

        // Set epic IDs if available
        if (entity.getEpics() != null && !entity.getEpics().isEmpty()) {
            dto.setEpicIds(entity.getEpics().stream()
                    .map(Epic::getId)
                    .collect(Collectors.toList()));
        } else {
            dto.setEpicIds(new ArrayList<>());
        }

        // Set sprint backlog IDs if available
        if (entity.getSprintBacklogs() != null && !entity.getSprintBacklogs().isEmpty()) {
            dto.setSprintBacklogIds(entity.getSprintBacklogs().stream()
                    .map(SprintBacklog::getId)
                    .collect(Collectors.toList()));
        } else {
            dto.setSprintBacklogIds(new ArrayList<>());
        }

        return dto;
    }

    public ProductBacklog dtoToEntity(ProductBacklogDTO dto) {
        if (dto == null) {
            return null;
        }

        ProductBacklog entity = new ProductBacklog();

        // Only set ID if it's an update (not a new entity)
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }

        entity.setTitle(dto.getTitle());

        // Set project if available
        if (dto.getProjectId() != null) {
            Project project = projectRepository.findById(dto.getProjectId())
                    .orElse(null);
            entity.setProject(project);
        }

        // Set epics if available
        if (dto.getEpicIds() != null && !dto.getEpicIds().isEmpty()) {
            List<Epic> epics = dto.getEpicIds().stream()
                    .map(epicId -> epicRepository.findById(Long.valueOf(epicId)).orElse(null))
                    .filter(epic -> epic != null)
                    .collect(Collectors.toList());
            entity.setEpics(epics);
        } else {
            entity.setEpics(new ArrayList<>());
        }

        // We don't set sprint backlogs here because that's managed separately
        entity.setSprintBacklogs(new ArrayList<>());

        return entity;
    }

    public List<ProductBacklogDTO> entitiesToDtos(List<ProductBacklog> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }
}