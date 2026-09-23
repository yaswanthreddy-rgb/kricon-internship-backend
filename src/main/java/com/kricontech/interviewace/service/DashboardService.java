package com.kricontech.interviewace.service;

import com.kricontech.interviewace.dto.ActivityResponse;
import com.kricontech.interviewace.dto.DashboardResponse;
import com.kricontech.interviewace.model.Activity;
import com.kricontech.interviewace.model.User;
import com.kricontech.interviewace.repository.ActivityRepository;
import com.kricontech.interviewace.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    public DashboardService(UserRepository userRepository, ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
    }

    public DashboardResponse getDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        List<ActivityResponse> recent = activityRepository
                .findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, 5))
                .stream()
                .map(a -> new ActivityResponse(a.getTitle(), a.getDescription(), a.getCreatedAt()))
                .toList();

        return new DashboardResponse(
                user.getName(),
                user.getDsaSolved(),
                user.getJavaQuestions(),
                user.getMockInterviews(),
                user.getResumeScore(),
                user.getJavaProgress(),
                user.getDsaProgress(),
                user.getPythonProgress(),
                recent
        );
    }

    public void logActivity(String email, String title, String description) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        activityRepository.save(new Activity(user, title, description));
    }
}
