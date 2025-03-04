package org.example.productbacklog.service;

import org.example.productbacklog.dto.ProjectDTO;
import org.example.productbacklog.entity.Project;
import java.util.List;

public interface ProjectService {
    Project addProject(Project project);
    Project updateProject(ProjectDTO projectDTO, int idProjet);
    Project getProject(int projectId);
    List<Project> getProjects();
    Project deleteProject(int projectId);
    Project getProjectByName(String projectName);
}