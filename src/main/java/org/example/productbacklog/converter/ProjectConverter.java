package org.example.productbacklog.converter;

import org.example.productbacklog.dto.ProjectDTO;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectConverter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductBacklogRepository productBacklogRepository;

    public ProjectDTO convertToDTO(Project entity) {
        if (entity == null) {
            return null;
        }

        return ProjectDTO.builder()
                .projectId(entity.getProjectId())
                .projectName(entity.getProjectName())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .productBacklogId(entity.getProductBacklog() != null ? entity.getProductBacklog().getId() : null)
                .build();
    }

    public List<ProjectDTO> convertToDTOList(List<Project> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Project convertToEntity(ProjectDTO dto) {
        if (dto == null) {
            return null;
        }

        Project entity = new Project();
        entity.setProjectId(dto.getProjectId());
        entity.setProjectName(dto.getProjectName());

        // Set user if userId is provided
        if (dto.getUserId() != null) {
            userRepository.findById(dto.getUserId())
                    .ifPresent(entity::setUser);
        }

        // Set product backlog if productBacklogId is provided
        if (dto.getProductBacklogId() != null) {
            productBacklogRepository.findById(dto.getProductBacklogId())
                    .ifPresent(entity::setProductBacklog);
        }

        return entity;
    }

    public Project updateEntityFromDTO(Project entity, ProjectDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        if (dto.getProjectName() != null) {
            entity.setProjectName(dto.getProjectName());
        }

        if (dto.getUserId() != null) {
            userRepository.findById(dto.getUserId())
                    .ifPresent(entity::setUser);
        }

        return entity;
    }
}