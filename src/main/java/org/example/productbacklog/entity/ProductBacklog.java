package org.example.productbacklog.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "product_backlog")
public class ProductBacklog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titreProductBL;

    @OneToMany(mappedBy = "productBacklog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Epic> epics;

    // Default constructor
    public ProductBacklog() {
    }

    // Constructor with all fields
    public ProductBacklog(Long id, String titreProductBL, List<Epic> epics) {
        this.id = id;
        this.titreProductBL = titreProductBL;
        this.epics = epics;
    }

    // Getter for titreProductBL
    public String getTitreProductBL() {
        return titreProductBL;
    }

    // Setter for titreProductBL
    public void setTitreProductBL(String titreProductBL) {
        this.titreProductBL = titreProductBL;
    }

    // Getter for id
    public Long getId() {
        return id;
    }

    // Setter for id
    public void setId(Long id) {
        this.id = id;
    }

    // Getter for epics
    public List<Epic> getEpics() {
        return epics;
    }

    // Setter for epics
    public void setEpics(List<Epic> epics) {
        this.epics = epics;
    }
}
