package org.example.productbacklog.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private Long userId;
    private Long taskId;
}
