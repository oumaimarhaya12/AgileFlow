package org.example.productbacklog.repository;

import org.example.productbacklog.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // You can define custom queries here if needed
}