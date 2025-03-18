package org.example.productbacklog.service.impl;

import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.entity.Statut;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.service.SprintBacklogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SprintBacklogServiceImpl implements SprintBacklogService {

    private final SprintBacklogRepository sprintBacklogRepository;
    private final UserStoryRepository userStoryRepository;

    @Autowired
    public SprintBacklogServiceImpl(SprintBacklogRepository sprintBacklogRepository, UserStoryRepository userStoryRepository) {
        this.sprintBacklogRepository = sprintBacklogRepository;
        this.userStoryRepository = userStoryRepository;
    }

    @Override
    @Transactional
    public SprintBacklog createSprintBacklog(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre du Sprint Backlog ne peut pas être vide");
        }

        SprintBacklog sprintBacklog = new SprintBacklog();
        sprintBacklog.setTitle(title);
        return sprintBacklogRepository.save(sprintBacklog);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SprintBacklog> getSprintBacklogById(Long id) {
        return sprintBacklogRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintBacklog> getAllSprintBacklogs() {
        return sprintBacklogRepository.findAllByOrderByIdDesc();
    }

    @Override
    @Transactional
    public SprintBacklog updateSprintBacklog(Long id, String title) {
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog non trouvé avec l'ID: " + id));

        if (title != null && !title.trim().isEmpty()) {
            sprintBacklog.setTitle(title);
        }

        return sprintBacklogRepository.save(sprintBacklog);
    }

    @Override
    @Transactional
    public void deleteSprintBacklog(Long id) {
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog non trouvé avec l'ID: " + id));

        // Détacher les User Stories du Sprint Backlog avant de le supprimer
        for (UserStory userStory : sprintBacklog.getUserStories()) {
            userStory.setSprintBacklog(null);
            userStoryRepository.save(userStory);
        }

        sprintBacklogRepository.delete(sprintBacklog);
    }

    @Override
    @Transactional
    public SprintBacklog addUserStoryToSprintBacklog(Long sprintBacklogId, Long userStoryId) {
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog non trouvé avec l'ID: " + sprintBacklogId));

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new EntityNotFoundException("User Story non trouvée avec l'ID: " + userStoryId));

        userStory.setSprintBacklog(sprintBacklog);
        userStoryRepository.save(userStory);

        // Rafraîchir le sprint backlog pour obtenir les changements
        return sprintBacklogRepository.findById(sprintBacklogId).orElseThrow();
    }

    @Override
    @Transactional
    public SprintBacklog removeUserStoryFromSprintBacklog(Long sprintBacklogId, Long userStoryId) {
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog non trouvé avec l'ID: " + sprintBacklogId));

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new EntityNotFoundException("User Story non trouvée avec l'ID: " + userStoryId));

        // Vérifier que l'user story appartient bien à ce sprint backlog
        if (userStory.getSprintBacklog() != null && userStory.getSprintBacklog().getId().equals(sprintBacklogId)) {
            userStory.setSprintBacklog(null);
            userStoryRepository.save(userStory);
        }

        // Rafraîchir le sprint backlog pour obtenir les changements
        return sprintBacklogRepository.findById(sprintBacklogId).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStory> getUserStoriesInSprintBacklog(Long sprintBacklogId) {
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog non trouvé avec l'ID: " + sprintBacklogId));

        return sprintBacklog.getUserStories();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Task.TaskStatus, Long> getTaskStatusSummary(Long sprintBacklogId) {
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog non trouvé avec l'ID: " + sprintBacklogId));

        // Récupérer toutes les tâches des user stories dans ce sprint backlog
        List<Task> tasks = sprintBacklog.getUserStories().stream()
                .flatMap(userStory -> userStory.getTasks().stream())
                .collect(Collectors.toList());

        // Compter le nombre de tâches par statut
        Map<Task.TaskStatus, Long> statusSummary = new HashMap<>();
        for (Task.TaskStatus status : Task.TaskStatus.values()) {
            long count = tasks.stream()
                    .filter(task -> status.equals(task.getStatus()))
                    .count();
            statusSummary.put(status, count);
        }

        return statusSummary;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Statut, Long> getUserStoryStatusSummary(Long sprintBacklogId) {
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog non trouvé avec l'ID: " + sprintBacklogId));

        // Compter le nombre d'user stories par statut
        Map<Statut, Long> statusSummary = new HashMap<>();
        for (Statut status : Statut.values()) {
            long count = sprintBacklog.getUserStories().stream()
                    .filter(userStory -> status.equals(userStory.getStatus()))
                    .count();
            statusSummary.put(status, count);
        }

        return statusSummary;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(Long sprintBacklogId, Task.TaskStatus status) {
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog non trouvé avec l'ID: " + sprintBacklogId));

        return sprintBacklog.getUserStories().stream()
                .flatMap(userStory -> userStory.getTasks().stream())
                .filter(task -> status.equals(task.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int countTotalUserStories(Long sprintBacklogId) {
        return sprintBacklogRepository.countUserStoriesBySprintBacklogId(sprintBacklogId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countTotalTasks(Long sprintBacklogId) {
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog non trouvé avec l'ID: " + sprintBacklogId));

        return sprintBacklog.getUserStories().stream()
                .mapToInt(userStory -> userStory.getTasks().size())
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateSprintProgress(Long sprintBacklogId) {
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint Backlog non trouvé avec l'ID: " + sprintBacklogId));

        List<Task> tasks = sprintBacklog.getUserStories().stream()
                .flatMap(userStory -> userStory.getTasks().stream())
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            return 0.0;
        }

        long completedTasks = tasks.stream()
                .filter(task -> Task.TaskStatus.FINISHED.equals(task.getStatus()))
                .count();

        return (double) completedTasks / tasks.size() * 100.0;
    }
}