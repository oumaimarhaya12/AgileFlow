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
public class ProductBacklogDTO {
    private Integer id;
    private String title;
    private List<Long> epicIds;
    private List<Long> sprintBacklogIds;
    private Long projectId;
}
