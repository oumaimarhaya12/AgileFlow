package org.example.productbacklog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_backlog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductBacklog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @OneToMany(mappedBy = "productBacklog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Epic> epics = new ArrayList<>();

    @OneToMany(mappedBy = "productBacklog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SprintBacklog> sprintBacklogs = new ArrayList<>();

    @OneToOne(optional = true)
    @JoinColumn(name = "project_id", nullable = true,
            foreignKey = @ForeignKey(name = "FK_PRODUCT_BACKLOG_PROJECT"))
    private Project project;

    // Constructor with fields (in addition to @AllArgsConstructor)
    public ProductBacklog(String title, List<Epic> epics, Project project) {
        this.title = title;
        if (epics != null) {
            this.epics = epics;
        }
        this.project = project;
    }

    // Custom setter for epics to maintain consistency
    public void setEpics(List<Epic> epics) {
        this.epics.clear();
        if (epics != null) {
            this.epics.addAll(epics);
        }
    }

    // Custom setter for sprintBacklogs to maintain consistency
    public void setSprintBacklogs(List<SprintBacklog> sprintBacklogs) {
        this.sprintBacklogs.clear();
        if (sprintBacklogs != null) {
            this.sprintBacklogs.addAll(sprintBacklogs);
        }
    }
}