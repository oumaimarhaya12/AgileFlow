package org.example.productbacklog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintBacklogDTO {
    private Long id;
    private String title;
    private List<Long> userStoryIds;
    private List<Long> sprintIds;
    private Integer productBacklogId;
}
