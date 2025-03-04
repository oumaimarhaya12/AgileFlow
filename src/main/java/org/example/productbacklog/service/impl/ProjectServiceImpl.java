package org.example.productbacklog.service.impl;

import org.example.productbacklog.dto.ProjectDTO;
import org.example.productbacklog.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.productbacklog.repository.ProjectRepository;
import org.example.productbacklog.service.ProjectService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public Project addProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public Project updateProject(ProjectDTO projectDTO, int projectId) {
        Optional<Project> projectToUpdate = projectRepository.findById(projectId);
        if (projectToUpdate.isEmpty()) {
            throw new IllegalStateException("Le projet n'existe pas");
        }

        Project existingProject = projectToUpdate.get();
        // Update the project name if it differs from the DTO's name
        if (!existingProject.getProjectName().equals(projectDTO.getNomProjet())) {
            existingProject.setProjectName(projectDTO.getNomProjet());
        }
        // Optionally, you can add more fields to update if needed, like description, etc.

        return projectRepository.save(existingProject);
    }

    @Override
    public Project getProject(int projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            throw new IllegalStateException("Le projet n'existe pas");
        }
        return projectOptional.get();
    }

    @Override
    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    @Override
    public Project deleteProject(int projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            throw new IllegalStateException("Le projet n'existe pas");
        }
        Project projectToDelete = projectOptional.get();
        projectRepository.delete(projectToDelete);
        return projectToDelete;
    }

    @Override
    public Project getProjectByName(String projectName) {
        Optional<Project> projectOptional = projectRepository.findFirstByProjectName(projectName);
        if (projectOptional.isEmpty()) {
            throw new IllegalStateException("Le projet n'existe pas");
        }
        return projectOptional.get();
    }
}