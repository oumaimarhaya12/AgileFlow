package org.example.productbacklog.converter;

import org.example.productbacklog.dto.UserDTO;
import org.example.productbacklog.entity.Comment;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserConverter {
    private static final Logger log = LoggerFactory.getLogger(UserConverter.class);

    /**
     * Converts a User entity to a UserDTO
     * @param entity The User entity to convert
     * @return The corresponding UserDTO
     */
    public UserDTO convertToDTO(User entity) {
        if (entity == null) {
            return null;
        }

        List<Long> projectIds = entity.getProjects() != null ?
                entity.getProjects().stream()
                        .map(Project::getProjectId)
                        .map(Long::valueOf)
                        .collect(Collectors.toList()) :
                List.of();

        List<Long> taskIds = entity.getTasks() != null ?
                entity.getTasks().stream()
                        .map(Task::getId)
                        .collect(Collectors.toList()) :
                List.of();

        List<Long> commentIds = entity.getComments() != null ?
                entity.getComments().stream()
                        .map(Comment::getId)
                        .collect(Collectors.toList()) :
                List.of();

        return UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .role(entity.getRole())
                .projectIds(projectIds)
                .taskIds(taskIds)
                .commentIds(commentIds)
                .build();
    }

    /**
     * Converts a list of User entities to a list of UserDTOs
     * @param entities The list of User entities to convert
     * @return The corresponding list of UserDTOs
     */
    public List<UserDTO> convertToDTOList(List<User> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a UserDTO to a User entity
     * Note: This method does not set relationships (projects, tasks, comments)
     * as these are typically managed through service methods
     * @param dto The UserDTO to convert
     * @return The corresponding User entity
     */
    public User convertToEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        log.info("Converting UserDTO to User: ID={}, Username={}, Email={}",
                dto.getId(), dto.getUsername(), dto.getEmail());

        return User.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .role(dto.getRole())
                .build();
    }

    /**
     * Updates an existing User entity with data from a UserDTO
     * Note: This method does not update relationships (projects, tasks, comments)
     * as these are typically managed through service methods
     * @param entity The User entity to update
     * @param dto The UserDTO containing the new data
     * @return The updated User entity
     */
    public User updateEntityFromDTO(User entity, UserDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        if (dto.getUsername() != null) {
            entity.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }

        if (dto.getRole() != null) {
            entity.setRole(dto.getRole());
        }

        return entity;
    }
}