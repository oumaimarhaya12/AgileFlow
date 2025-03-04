package org.example.productbacklog.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_backlog")
public class ProductBacklog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @OneToMany(mappedBy = "productBacklog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Epic> epics = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public ProductBacklog(String title, List<Epic> epics, Project project) {
        this.title = title;
        if (epics != null) {
            this.epics = epics;
        }
        this.project = project;
    }

    public ProductBacklog() {
    }

    // Getter and Setter methods for 'id'
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Getter and Setter methods for 'title'
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and Setter methods for 'epics'
    public List<Epic> getEpics() {
        return epics;
    }

    public void setEpics(List<Epic> epics) {
        this.epics.clear();
        if (epics != null) {
            this.epics.addAll(epics);
        }
    }

    // Getter and Setter methods for 'project'
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}