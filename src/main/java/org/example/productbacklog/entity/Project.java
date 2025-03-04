package org.example.productbacklog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    @Column(nullable = false)
    private String projectName;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductBacklog productBacklog;

    public Project(String projectName) {
        this.projectName = projectName;
    }

    // Helper method to manage bidirectional relationship
    public void setProductBacklog(ProductBacklog productBacklog) {
        this.productBacklog = productBacklog;
        if (productBacklog != null) {
            productBacklog.setProject(this);
        }
    }
}