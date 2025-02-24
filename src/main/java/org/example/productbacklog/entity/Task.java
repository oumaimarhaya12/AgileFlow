package org.example.productbacklog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titreTask;

    @Column(length = 1000)
    private String descTask;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statutT;

    @ManyToOne
    @JoinColumn(name = "user_story_id")
    private UserStory userStory;
}