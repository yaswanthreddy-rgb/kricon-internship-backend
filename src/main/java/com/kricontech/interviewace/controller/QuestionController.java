package com.kricontech.interviewace.controller;

import com.kricontech.interviewace.dto.QuestionCompletionRequest;
import com.kricontech.interviewace.dto.QuestionResponse;
import com.kricontech.interviewace.service.DashboardService;
import com.kricontech.interviewace.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final DashboardService dashboardService;

    public QuestionController(QuestionService questionService, DashboardService dashboardService) {
        this.questionService = questionService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            Authentication auth,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(questionService.getQuestions(auth.getName(), category));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeQuestion(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody QuestionCompletionRequest request
    ) {
        Optional<QuestionResponse> newlyCompleted =
                questionService.completeQuestion(auth.getName(), request.getCategory(), id);

        // Only log activity the first time, so repeat clicks can't spam the feed.
        newlyCompleted.ifPresent(q ->
                dashboardService.logActivity(auth.getName(), q.getTitle(), "Completed " + q.getCategory() + " question"));

        return ResponseEntity.ok().build();
    }
}
