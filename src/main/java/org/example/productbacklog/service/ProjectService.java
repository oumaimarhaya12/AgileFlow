package org.example.productbacklog.service;

import org.example.productbacklog.dto.ProjectDTO;
import java.util.List;
import java.util.Map;

public interface ProjectService {
    ProjectDTO addProject(ProjectDTO projectDTO);
    ProjectDTO addProjectWithOwner(ProjectDTO projectDTO, Long userId);
    ProjectDTO updateProject(ProjectDTO projectDTO, int projectId);
    ProjectDTO getProject(int projectId);
    List<ProjectDTO> getProjects();
    List<ProjectDTO> getProjectsByUser(Long userId);
    boolean deleteProject(int projectId);
    ProjectDTO getProjectByName(String projectName);
    boolean linkProjectToBacklog(Integer projectId, Integer backlogId);
    boolean unlinkProjectFromBacklog(Integer projectId);
    Map<String, Integer> getProjectStatistics();
    boolean assignProjectToUser(Integer projectId, Long userId);
    boolean removeUserFromProject(Integer projectId);
}