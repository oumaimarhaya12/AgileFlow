package org.example.productbacklog.repository;

import org.example.productbacklog.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    // Changed method names to match the entity field name (projectName)
    Optional<Project> findFirstByProjectName(String projectName);
    Project findByProjectName(String projectName);
}