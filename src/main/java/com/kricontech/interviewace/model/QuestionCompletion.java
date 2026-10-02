package com.kricontech.interviewace.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "question_completion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"})
)
public class QuestionCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDateTime completedAt = LocalDateTime.now();

    public QuestionCompletion() {}

    public QuestionCompletion(User user, Long questionId, String category) {
        this.user = user;
        this.questionId = questionId;
        this.category = category;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Long getQuestionId() { return questionId; }
    public String getCategory() { return category; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
