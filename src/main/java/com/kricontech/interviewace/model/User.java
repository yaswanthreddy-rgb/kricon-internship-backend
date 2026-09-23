package com.kricontech.interviewace.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hash — never store plain text

    // Dashboard stats — all start at 0 for a new account
    private int dsaSolved = 0;
    private int javaQuestions = 0;
    private int mockInterviews = 0;
    private int resumeScore = 0;

    private int javaProgress = 0;
    private int dsaProgress = 0;
    private int pythonProgress = 0;

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getDsaSolved() { return dsaSolved; }
    public void setDsaSolved(int dsaSolved) { this.dsaSolved = dsaSolved; }

    public int getJavaQuestions() { return javaQuestions; }
    public void setJavaQuestions(int javaQuestions) { this.javaQuestions = javaQuestions; }

    public int getMockInterviews() { return mockInterviews; }
    public void setMockInterviews(int mockInterviews) { this.mockInterviews = mockInterviews; }

    public int getResumeScore() { return resumeScore; }
    public void setResumeScore(int resumeScore) { this.resumeScore = resumeScore; }

    public int getJavaProgress() { return javaProgress; }
    public void setJavaProgress(int javaProgress) { this.javaProgress = javaProgress; }

    public int getDsaProgress() { return dsaProgress; }
    public void setDsaProgress(int dsaProgress) { this.dsaProgress = dsaProgress; }

    public int getPythonProgress() { return pythonProgress; }
    public void setPythonProgress(int pythonProgress) { this.pythonProgress = pythonProgress; }
}
