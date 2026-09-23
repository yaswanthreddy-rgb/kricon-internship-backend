package com.kricontech.interviewace.dto;

import java.util.List;

public class DashboardResponse {
    private String name;
    private int dsaSolved;
    private int javaQuestions;
    private int mockInterviews;
    private int resumeScore;
    private int javaProgress;
    private int dsaProgress;
    private int pythonProgress;
    private List<ActivityResponse> recentActivity;

    public DashboardResponse(String name, int dsaSolved, int javaQuestions, int mockInterviews,
                              int resumeScore, int javaProgress, int dsaProgress, int pythonProgress,
                              List<ActivityResponse> recentActivity) {
        this.name = name;
        this.dsaSolved = dsaSolved;
        this.javaQuestions = javaQuestions;
        this.mockInterviews = mockInterviews;
        this.resumeScore = resumeScore;
        this.javaProgress = javaProgress;
        this.dsaProgress = dsaProgress;
        this.pythonProgress = pythonProgress;
        this.recentActivity = recentActivity;
    }

    public String getName() { return name; }
    public int getDsaSolved() { return dsaSolved; }
    public int getJavaQuestions() { return javaQuestions; }
    public int getMockInterviews() { return mockInterviews; }
    public int getResumeScore() { return resumeScore; }
    public int getJavaProgress() { return javaProgress; }
    public int getDsaProgress() { return dsaProgress; }
    public int getPythonProgress() { return pythonProgress; }
    public List<ActivityResponse> getRecentActivity() { return recentActivity; }
}
