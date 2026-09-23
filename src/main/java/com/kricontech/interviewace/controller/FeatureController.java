package com.kricontech.interviewace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/features")
public class FeatureController {

    // TODO: replace this stub with a real call to an LLM API (OpenAI/Gemini/Claude)
    // once you're ready — pass the resume text in and return real feedback.
    @PostMapping("/resume-analyzer")
    public ResponseEntity<Map<String, Object>> analyzeResume(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(Map.of(
                "score", 0,
                "message", "Resume analysis isn't wired up to an AI model yet — this endpoint is ready for it."
        ));
    }

    // TODO: same here — this is where a mock-interview question/answer loop backed by an LLM would live.
    @PostMapping("/mock-interview")
    public ResponseEntity<Map<String, Object>> mockInterview(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(Map.of(
                "message", "Mock interview isn't wired up to an AI model yet — this endpoint is ready for it."
        ));
    }
}
