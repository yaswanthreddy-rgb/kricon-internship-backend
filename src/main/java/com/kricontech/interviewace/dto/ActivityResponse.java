package com.kricontech.interviewace.dto;

import java.time.LocalDateTime;

public class ActivityResponse {
    private String title;
    private String description;
    private LocalDateTime createdAt;

    public ActivityResponse(String title, String description, LocalDateTime createdAt) {
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
