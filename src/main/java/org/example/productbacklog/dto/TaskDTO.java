package org.example.productbacklog.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime dueDate;
    private int priority;
    private int estimatedHours;
    private int loggedHours;
    private Long userStoryId;
    private Long assignedUserId;
}
