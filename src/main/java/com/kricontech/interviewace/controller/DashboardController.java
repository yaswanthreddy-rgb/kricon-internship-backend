package com.kricontech.interviewace.controller;

import com.kricontech.interviewace.dto.DashboardResponse;
import com.kricontech.interviewace.dto.LogActivityRequest;
import com.kricontech.interviewace.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // The JWT filter puts the logged-in user's email into Authentication as the "name".
    @GetMapping("/me")
    public ResponseEntity<DashboardResponse> myDashboard(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getDashboard(auth.getName()));
    }

    @PostMapping("/activity")
    public ResponseEntity<Void> logActivity(
            Authentication auth,
            @Valid @RequestBody LogActivityRequest request
    ) {
        dashboardService.logActivity(auth.getName(), request.getTitle(), request.getDescription());
        return ResponseEntity.ok().build();
    }
}
