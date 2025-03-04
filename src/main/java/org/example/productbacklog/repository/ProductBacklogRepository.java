package org.example.productbacklog.repository;

import org.example.productbacklog.entity.ProductBacklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductBacklogRepository extends JpaRepository<ProductBacklog, Integer> {
    // Change return type to Optional<ProductBacklog> and use findFirstByTitle
    Optional<ProductBacklog> findFirstByTitle(String title);
}