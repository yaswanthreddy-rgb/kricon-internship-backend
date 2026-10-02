package com.kricontech.interviewace.dto;

public class QuestionResponse {
    private final Long id;
    private final String category;
    private final String title;
    private final String question;
    private final boolean completed;

    public QuestionResponse(Long id, String category, String title, String question) {
        this(id, category, title, question, false);
    }

    public QuestionResponse(Long id, String category, String title, String question, boolean completed) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.question = question;
        this.completed = completed;
    }

    public QuestionResponse withCompleted(boolean completed) {
        return new QuestionResponse(id, category, title, question, completed);
    }

    public Long getId() { return id; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getQuestion() { return question; }
    public boolean isCompleted() { return completed; }
}
