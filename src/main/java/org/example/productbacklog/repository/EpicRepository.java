package org.example.productbacklog.repository;

import org.example.productbacklog.entity.Epic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Long> {
    // You can define custom queries here if needed
}
