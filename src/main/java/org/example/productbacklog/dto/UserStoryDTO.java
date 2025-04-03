package org.example.productbacklog.dto;

import lombok.Data;

@Data
public class UserStoryDTO {

    private String title;
    private String asA;
    private String iWant;
    private String soThat;
    private String description;
    private String acceptanceCriteria;
    private int priority;
    private String status;
    private Integer epicId;
    private Integer productBacklogId;
    private Long sprintBacklogId;
}
