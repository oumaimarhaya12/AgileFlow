package org.example.productbacklog.dto;

import lombok.Data;

@Data
public class EpicDTO {
    private Integer id;
    private String title;
    private Integer productBacklogId;
}
