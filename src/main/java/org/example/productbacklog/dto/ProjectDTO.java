package org.example.productbacklog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectDTO {
    private int idProjet;
    private String nomProjet;

    public ProjectDTO(int idProjet, String nomProjet) {
        this.idProjet = idProjet;
        this.nomProjet = nomProjet;
    }
}