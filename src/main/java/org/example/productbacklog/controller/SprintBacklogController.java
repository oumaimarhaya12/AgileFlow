package org.example.productbacklog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.productbacklog.dto.SprintBacklogDTO;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.service.SprintBacklogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sprint-backlogs")
@Tag(name = "Sprint-Backlog-Controller", description = "Gestion des Sprint Backlogs")
public class SprintBacklogController {

    private final SprintBacklogService sprintBacklogService;

    public SprintBacklogController(SprintBacklogService sprintBacklogService) {
        this.sprintBacklogService = sprintBacklogService;
    }

    @PostMapping("/create")
    @Operation(summary = "Créer un Sprint Backlog")
    public ResponseEntity<SprintBacklogDTO> createSprintBacklog(@RequestParam String title) {
        return ResponseEntity.ok(sprintBacklogService.createSprintBacklog(title));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un Sprint Backlog par ID")
    public ResponseEntity<SprintBacklogDTO> getSprintBacklogById(@PathVariable Long id) {
        Optional<SprintBacklogDTO> sprintBacklog = sprintBacklogService.getSprintBacklogById(id);
        return sprintBacklog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    @Operation(summary = "Obtenir tous les Sprint Backlogs")
    public ResponseEntity<List<SprintBacklogDTO>> getAllSprintBacklogs() {
        return ResponseEntity.ok(sprintBacklogService.getAllSprintBacklogs());
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Mettre à jour un Sprint Backlog")
    public ResponseEntity<SprintBacklogDTO> updateSprintBacklog(@PathVariable Long id, @RequestParam String title) {
        return ResponseEntity.ok(sprintBacklogService.updateSprintBacklog(id, title));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Supprimer un Sprint Backlog")
    public ResponseEntity<Void> deleteSprintBacklog(@PathVariable Long id) {
        sprintBacklogService.deleteSprintBacklog(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sprintBacklogId}/add-user-story/{userStoryId}")
    @Operation(summary = "Ajouter une User Story à un Sprint Backlog")
    public ResponseEntity<SprintBacklogDTO> addUserStoryToSprintBacklog(@PathVariable Long sprintBacklogId, @PathVariable Long userStoryId) {
        return ResponseEntity.ok(sprintBacklogService.addUserStoryToSprintBacklog(sprintBacklogId, userStoryId));
    }

    @GetMapping("/{sprintBacklogId}/user-stories")
    @Operation(summary = "Obtenir les User Stories d'un Sprint Backlog")
    public ResponseEntity<List<UserStory>> getUserStoriesInSprintBacklog(@PathVariable Long sprintBacklogId) {
        return ResponseEntity.ok(sprintBacklogService.getUserStoriesInSprintBacklog(sprintBacklogId));
    }

    @GetMapping("/{sprintBacklogId}/tasks-by-status")
    @Operation(summary = "Obtenir les tâches d'un Sprint Backlog par statut")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable Long sprintBacklogId, @RequestParam Task.TaskStatus status) {
        return ResponseEntity.ok(sprintBacklogService.getTasksByStatus(sprintBacklogId, status));
    }

    @GetMapping("/{sprintBacklogId}/progress")
    @Operation(summary = "Calculer la progression d'un Sprint Backlog")
    public ResponseEntity<Double> calculateSprintProgress(@PathVariable Long sprintBacklogId) {
        return ResponseEntity.ok(sprintBacklogService.calculateSprintProgress(sprintBacklogId));
    }
}