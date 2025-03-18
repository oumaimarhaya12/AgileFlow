package org.example.productbacklog.repository;

import org.example.productbacklog.entity.SprintBacklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintBacklogRepository extends JpaRepository<SprintBacklog, Long> {

    // Méthode pour trouver un sprint backlog par son titre
    SprintBacklog findByTitle(String title);

    // Méthode pour obtenir tous les sprint backlogs triés par ID (du plus récent au plus ancien)
    List<SprintBacklog> findAllByOrderByIdDesc();

    // Méthode pour compter le nombre d'user stories dans un sprint backlog
    @Query("SELECT COUNT(us) FROM UserStory us WHERE us.sprintBacklog.id = :sprintBacklogId")
    int countUserStoriesBySprintBacklogId(Long sprintBacklogId);

    // Méthode pour trouver les sprint backlogs qui ont des user stories avec un statut spécifique
    @Query("SELECT DISTINCT sb FROM SprintBacklog sb JOIN sb.userStories us WHERE us.status = :status")
    List<SprintBacklog> findByUserStoriesStatus(org.example.productbacklog.entity.Statut status);
}
