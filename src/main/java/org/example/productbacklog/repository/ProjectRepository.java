package org.example.productbacklog.repository;

import org.example.productbacklog.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    // Change return type to Optional<Project> and use findFirstByProjectName
    Optional<Project> findFirstByProjectName(String projectName);
}