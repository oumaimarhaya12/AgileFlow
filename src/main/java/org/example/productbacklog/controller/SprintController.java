package org.example.productbacklog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.productbacklog.entity.Sprint;
import org.example.productbacklog.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sprints")
@Tag(name = "Sprint-Controller", description = "API pour gérer les sprints")
public class SprintController {

    private final SprintService sprintService;

    @Autowired
    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping
    @Operation(summary = "Créer un sprint", description = "Crée un nouveau sprint et l'associe à un backlog de sprint")
    public ResponseEntity<Sprint> createSprint(
            @RequestParam String name,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Long sprintBacklogId) {
        Sprint sprint = sprintService.createSprint(name, startDate, endDate, sprintBacklogId);
        return ResponseEntity.ok(sprint);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un sprint par ID", description = "Récupère un sprint en fonction de son identifiant")
    public ResponseEntity<Optional<Sprint>> getSprintById(@PathVariable Long id) {
        return ResponseEntity.ok(sprintService.getSprintById(id));
    }

    @GetMapping
    @Operation(summary = "Obtenir tous les sprints", description = "Récupère la liste de tous les sprints")
    public ResponseEntity<List<Sprint>> getAllSprints() {
        return ResponseEntity.ok(sprintService.getAllSprints());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un sprint", description = "Modifie les informations d'un sprint existant")
    public ResponseEntity<Sprint> updateSprint(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(sprintService.updateSprint(id, name, startDate, endDate));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un sprint", description = "Supprime un sprint par son identifiant")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/backlog/{sprintBacklogId}")
    @Operation(summary = "Obtenir les sprints d'un backlog de sprint", description = "Récupère tous les sprints associés à un backlog de sprint donné")
    public ResponseEntity<List<Sprint>> getSprintsBySprintBacklogId(@PathVariable Long sprintBacklogId) {
        return ResponseEntity.ok(sprintService.getSprintsBySprintBacklogId(sprintBacklogId));
    }

    @GetMapping("/active")
    @Operation(summary = "Obtenir les sprints actifs", description = "Récupère tous les sprints actifs à une date donnée")
    public ResponseEntity<List<Sprint>> getActiveSprintsByDate(@RequestParam(required = false) LocalDate date) {
        return ResponseEntity.ok(sprintService.getActiveSprintsByDate(date));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Obtenir les sprints à venir", description = "Récupère tous les sprints à venir")
    public ResponseEntity<List<Sprint>> getUpcomingSprints() {
        return ResponseEntity.ok(sprintService.getUpcomingSprints());
    }

    @GetMapping("/completed")
    @Operation(summary = "Obtenir les sprints terminés", description = "Récupère tous les sprints terminés")
    public ResponseEntity<List<Sprint>> getCompletedSprints() {
        return ResponseEntity.ok(sprintService.getCompletedSprints());
    }

    @PostMapping("/{sprintId}/assign/{sprintBacklogId}")
    @Operation(summary = "Assigner un sprint à un backlog", description = "Associe un sprint à un backlog de sprint")
    public ResponseEntity<Sprint> assignSprintToSprintBacklog(
            @PathVariable Long sprintId,
            @PathVariable Long sprintBacklogId) {
        return ResponseEntity.ok(sprintService.assignSprintToSprintBacklog(sprintId, sprintBacklogId));
    }

    @PostMapping("/{sprintId}/remove-backlog")
    @Operation(summary = "Dissocier un sprint d'un backlog", description = "Retire un sprint de son backlog de sprint actuel")
    public ResponseEntity<Sprint> removeSprintFromSprintBacklog(@PathVariable Long sprintId) {
        return ResponseEntity.ok(sprintService.removeSprintFromSprintBacklog(sprintId));
    }
}
