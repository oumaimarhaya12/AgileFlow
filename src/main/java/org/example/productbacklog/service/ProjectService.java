package org.example.productbacklog.service;

import org.example.productbacklog.dto.ProjectDTO;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.User;
import java.util.List;
import java.util.Map;

public interface ProjectService {
    Project addProject(Project project);
    Project addProjectWithOwner(Project project, Long userId);
    Project updateProject(ProjectDTO projectDTO, int idProjet);
    Project getProject(int projectId);
    List<Project> getProjects();
    List<Project> getProjectsByUser(Long userId);
    Project deleteProject(int projectId);
    Project getProjectByName(String projectName);
    boolean linkProjectToBacklog(Integer projectId, Integer backlogId);
    boolean unlinkProjectFromBacklog(Integer projectId);
    Map<String, Integer> getProjectStatistics();
    boolean assignProjectToUser(Integer projectId, Long userId);
    boolean removeUserFromProject(Integer projectId);
}