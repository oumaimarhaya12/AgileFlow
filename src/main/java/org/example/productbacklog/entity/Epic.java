package org.example.productbacklog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "epic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Epic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "product_backlog_id", nullable = false)
    private ProductBacklog productBacklog;

    @OneToMany(mappedBy = "epic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStory> userStories = new ArrayList<>();

    // Custom constructor without userStories list
    public Epic(Integer id, String title, ProductBacklog productBacklog) {
        this.id = id;
        this.title = title;
        this.productBacklog = productBacklog;
    }

    // Custom setter for userStories to maintain consistency
    public void setUserStories(List<UserStory> userStories) {
        this.userStories.clear();
        if (userStories != null) {
            this.userStories.addAll(userStories);
        }
    }
}