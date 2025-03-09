package org.example.productbacklog.dto;

public class ProjectDTO {
    private Integer idProjet;
    private String projectName;

    // Constructor
    public ProjectDTO(Integer idProjet, String projectName) {
        this.idProjet = idProjet;
        this.projectName = projectName;
    }

    // Getters and Setters
    public Integer getIdProjet() {
        return idProjet;
    }

    public void setIdProjet(Integer idProjet) {
        this.idProjet = idProjet;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}