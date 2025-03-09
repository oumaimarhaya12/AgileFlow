package org.example.productbacklog.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_story")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Add this annotation to enable the builder pattern
public class UserStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String asA; // Who will benefit from the feature

    @Column(length = 500)
    private String iWant; // What they want to accomplish

    @Column(length = 500)
    private String soThat; // Why they want to accomplish it / value

    @Column(length = 1000)
    private String description;

    @Column(length = 1000)
    private String acceptanceCriteria;

    private int priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut status;

    @ManyToOne
    @JoinColumn(name = "epic_id", nullable = true,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT,
                    foreignKeyDefinition = "FOREIGN KEY (epic_id) REFERENCES epic (id) ON DELETE SET NULL"))
    private Epic epic;

    @ManyToOne
    @JoinColumn(name = "product_backlog_id", nullable = true)
    private ProductBacklog productBacklog;

    @ManyToOne
    @JoinColumn(name = "sprint_backlog_id", nullable = true)
    private SprintBacklog sprintBacklog;

    @OneToMany(mappedBy = "userStory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default  // Add this to initialize the list when using builder
    private List<Task> tasks = new ArrayList<>();

    // Custom setter for tasks to maintain consistency
    public void setTasks(List<Task> tasks) {
        this.tasks.clear();
        if (tasks != null) {
            this.tasks.addAll(tasks);
        }
    }
}