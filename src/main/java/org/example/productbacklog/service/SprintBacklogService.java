package org.example.productbacklog.service;

import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.entity.Statut;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.UserStory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SprintBacklogService {

    // Méthodes CRUD de base
    SprintBacklog createSprintBacklog(String title);

    Optional<SprintBacklog> getSprintBacklogById(Long id);

    List<SprintBacklog> getAllSprintBacklogs();

    SprintBacklog updateSprintBacklog(Long id, String title);

    void deleteSprintBacklog(Long id);

    // Méthodes pour gérer les User Stories dans un Sprint Backlog
    SprintBacklog addUserStoryToSprintBacklog(Long sprintBacklogId, Long userStoryId);

    SprintBacklog removeUserStoryFromSprintBacklog(Long sprintBacklogId, Long userStoryId);

    List<UserStory> getUserStoriesInSprintBacklog(Long sprintBacklogId);

    // Méthodes pour suivre l'état des tâches
    Map<Task.TaskStatus, Long> getTaskStatusSummary(Long sprintBacklogId);

    Map<Statut, Long> getUserStoryStatusSummary(Long sprintBacklogId);

    List<Task> getTasksByStatus(Long sprintBacklogId, Task.TaskStatus status);

    // Méthodes pour les statistiques et reporting
    int countTotalUserStories(Long sprintBacklogId);

    int countTotalTasks(Long sprintBacklogId);

    double calculateSprintProgress(Long sprintBacklogId);
}