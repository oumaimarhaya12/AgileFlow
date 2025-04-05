package org.example.productbacklog.controller;

import org.example.productbacklog.dto.ProjectDTO;
import org.example.productbacklog.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.addProject(projectDTO));
    }

    @PostMapping("/with-owner")
    public ResponseEntity<ProjectDTO> createProjectWithOwner(@RequestBody ProjectDTO projectDTO) {
        if (projectDTO.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(projectService.addProjectWithOwner(projectDTO, projectDTO.getUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable int id, @RequestBody ProjectDTO projectDTO) {
        ProjectDTO updatedProject = projectService.updateProject(projectDTO, id);
        if (updatedProject == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedProject);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable int id) {
        ProjectDTO projectDTO = projectService.getProject(id);
        if (projectDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(projectDTO);
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getProjects());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProjectDTO>> getProjectsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(projectService.getProjectsByUser(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable int id) {
        boolean deleted = projectService.deleteProject(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/name/{projectName}")
    public ResponseEntity<ProjectDTO> getProjectByName(@PathVariable String projectName) {
        ProjectDTO projectDTO = projectService.getProjectByName(projectName);
        if (projectDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(projectDTO);
    }

    @PutMapping("/{projectId}/assign-user/{userId}")
    public ResponseEntity<Void> assignUserToProject(
            @PathVariable Integer projectId,
            @PathVariable Long userId) {
        boolean assigned = projectService.assignProjectToUser(projectId, userId);
        return assigned ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{projectId}/remove-user")
    public ResponseEntity<Void> removeUserFromProject(@PathVariable Integer projectId) {
        boolean removed = projectService.removeUserFromProject(projectId);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{projectId}/link-backlog/{backlogId}")
    public ResponseEntity<Void> linkProjectToBacklog(
            @PathVariable Integer projectId,
            @PathVariable Integer backlogId) {
        boolean linked = projectService.linkProjectToBacklog(projectId, backlogId);
        return linked ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{projectId}/unlink-backlog")
    public ResponseEntity<Void> unlinkProjectFromBacklog(@PathVariable Integer projectId) {
        boolean unlinked = projectService.unlinkProjectFromBacklog(projectId);
        return unlinked ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Integer>> getProjectStatistics() {
        return ResponseEntity.ok(projectService.getProjectStatistics());
    }
}