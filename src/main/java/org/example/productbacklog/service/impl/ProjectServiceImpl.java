package org.example.productbacklog.service.impl;

import org.example.productbacklog.dto.ProjectDTO;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.repository.ProjectRepository;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProductBacklogRepository productBacklogRepository;

    @Override
    @Transactional
    public Project addProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project updateProject(ProjectDTO projectDTO, int idProjet) {
        Optional<Project> existingProject = projectRepository.findById(idProjet);
        if (existingProject.isPresent()) {
            Project project = existingProject.get();
            project.setProjectName(projectDTO.getProjectName());
            return projectRepository.save(project);
        }
        return null;
    }

    @Override
    public Project getProject(int projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }

    @Override
    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    @Override
    @Transactional
    public Project deleteProject(int projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isPresent()) {
            projectRepository.delete(project.get());
            return project.get();
        }
        return null;
    }

    @Override
    public Project getProjectByName(String projectName) {
        return projectRepository.findByProjectName(projectName);
    }

    @Override
    @Transactional
    public boolean linkProjectToBacklog(Integer projectId, Integer backlogId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        Optional<ProductBacklog> backlogOptional = productBacklogRepository.findById(backlogId);

        if (projectOptional.isPresent() && backlogOptional.isPresent()) {
            Project project = projectOptional.get();
            ProductBacklog backlog = backlogOptional.get();

            project.setProductBacklog(backlog);
            backlog.setProject(project);

            projectRepository.save(project);
            productBacklogRepository.save(backlog);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean unlinkProjectFromBacklog(Integer projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            ProductBacklog backlog = project.getProductBacklog();

            if (backlog != null) {
                backlog.setProject(null);
                project.setProductBacklog(null);

                productBacklogRepository.save(backlog);
                projectRepository.save(project);
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Integer> getProjectStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        List<Project> projects = projectRepository.findAll();

        int totalProjects = projects.size();
        int projectsWithBacklog = (int) projects.stream()
                .filter(p -> p.getProductBacklog() != null)
                .count();
        int projectsWithoutBacklog = totalProjects - projectsWithBacklog;

        stats.put("totalProjects", totalProjects);
        stats.put("projectsWithBacklog", projectsWithBacklog);
        stats.put("projectsWithoutBacklog", projectsWithoutBacklog);

        return stats;
    }
}