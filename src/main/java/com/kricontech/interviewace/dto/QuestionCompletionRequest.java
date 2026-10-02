package com.kricontech.interviewace.dto;

import jakarta.validation.constraints.NotBlank;

public class QuestionCompletionRequest {
    @NotBlank(message = "Category is required")
    private String category;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
