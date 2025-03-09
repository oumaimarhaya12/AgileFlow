package org.example.productbacklog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sprint_backlog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SprintBacklog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @OneToMany(mappedBy = "sprintBacklog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStory> userStories = new ArrayList<>();

    // Custom setter for userStories to maintain consistency
    public void setUserStories(List<UserStory> userStories) {
        this.userStories.clear();
        if (userStories != null) {
            this.userStories.addAll(userStories);
        }
    }
}