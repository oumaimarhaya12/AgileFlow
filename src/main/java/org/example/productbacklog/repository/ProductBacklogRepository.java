package org.example.productbacklog.repository;

import org.example.productbacklog.entity.ProductBacklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductBacklogRepository extends JpaRepository<ProductBacklog, Integer> {
    Optional<ProductBacklog> findFirstByTitle(String title);

    // Changed to match Project entity field name (projectId)
    List<ProductBacklog> findByProjectProjectId(int projectId);
}