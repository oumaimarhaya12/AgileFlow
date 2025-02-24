package org.example.productbacklog.repository;

import org.example.productbacklog.entity.SprintBacklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SprintBacklogRepository extends JpaRepository<SprintBacklog, Long> {
    // You can define custom queries here if needed
}
