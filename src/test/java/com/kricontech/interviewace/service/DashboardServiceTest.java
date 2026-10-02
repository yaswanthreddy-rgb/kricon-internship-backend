package com.kricontech.interviewace.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.kricontech.interviewace.dto.DashboardResponse;
import com.kricontech.interviewace.model.Activity;
import com.kricontech.interviewace.model.User;
import com.kricontech.interviewace.repository.ActivityRepository;
import com.kricontech.interviewace.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardMapsUserStatisticsAndRecentActivity() {
        User user = new User("Casey", "casey@example.com", "hash");
        user.setDsaSolved(3);
        user.setJavaQuestions(2);
        user.setMockInterviews(1);
        user.setResumeScore(80);
        user.setJavaProgress(40);
        user.setDsaProgress(75);
        user.setPythonProgress(10);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 2, 3, 4);
        Activity activity = new Activity(user, "Completed a question", "Two Sum");
        activity.setCreatedAt(createdAt);
        when(userRepository.findByEmail("casey@example.com")).thenReturn(Optional.of(user));
        when(activityRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, 5)))
                .thenReturn(List.of(activity));

        DashboardResponse response = dashboardService.getDashboard("casey@example.com");

        assertEquals("Casey", response.getName());
        assertEquals(3, response.getDsaSolved());
        assertEquals(2, response.getJavaQuestions());
        assertEquals(1, response.getMockInterviews());
        assertEquals(80, response.getResumeScore());
        assertEquals(40, response.getJavaProgress());
        assertEquals(75, response.getDsaProgress());
        assertEquals(10, response.getPythonProgress());
        assertEquals(1, response.getRecentActivity().size());
        assertEquals("Completed a question", response.getRecentActivity().get(0).getTitle());
        assertEquals("Two Sum", response.getRecentActivity().get(0).getDescription());
        assertEquals(createdAt, response.getRecentActivity().get(0).getCreatedAt());
        verify(activityRepository).findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, 5));
    }

    @Test
    void getDashboardRejectsUnknownUser() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertEquals("User not found.", assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getDashboard("missing@example.com")).getMessage());
    }

    @Test
    void logActivitySavesActivityForUser() {
        User user = new User("Casey", "casey@example.com", "hash");
        when(userRepository.findByEmail("casey@example.com")).thenReturn(Optional.of(user));

        dashboardService.logActivity("casey@example.com", "Completed a question", "Two Sum");

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void logActivityRejectsUnknownUser() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertEquals("User not found.", assertThrows(IllegalArgumentException.class,
                () -> dashboardService.logActivity("missing@example.com", "title", "description")).getMessage());
    }
}