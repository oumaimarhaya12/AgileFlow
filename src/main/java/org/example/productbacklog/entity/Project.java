package org.example.productbacklog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id") // Match foreign key definition
    private Integer projectId; // Changed from id to projectId to match DTO

    @Column(name = "project_name", nullable = false) // Added column name for clarity
    private String projectName; // Changed from name to projectName to match DTO and repository

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductBacklog productBacklog;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Additional constructor with just the name
    public Project(String projectName) {
        this.projectName = projectName;
    }
}