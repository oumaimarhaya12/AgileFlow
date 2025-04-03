package org.example.productbacklog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.productbacklog.entity.User;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private User.Role role;
    private List<Long> projectIds;
    private List<Long> taskIds;
    private List<Long> commentIds;
}
