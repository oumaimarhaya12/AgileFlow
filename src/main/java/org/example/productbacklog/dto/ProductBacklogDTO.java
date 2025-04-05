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
    private List<Integer> epicIds;  // Changed from Long to Integer
    private List<Long> sprintBacklogIds;
    private Integer projectId;  // Changed from Long to Integer
}