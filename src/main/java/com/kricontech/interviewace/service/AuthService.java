package com.kricontech.interviewace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kricontech.interviewace.dto.*;
import com.kricontech.interviewace.model.User;
import com.kricontech.interviewace.repository.UserRepository;
import com.kricontech.interviewace.security.JwtUtil;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.oauth.google.client-id:}")
    private String googleClientId;

    @Value("${app.oauth.google.client-secret:}")
    private String googleClientSecret;

    @Value("${app.oauth.google.redirect-uri:http://localhost:8080/api/auth/google/callback}")
    private String googleRedirectUri;

    @Value("${app.oauth.github.client-id:}")
    private String githubClientId;

    @Value("${app.oauth.github.client-secret:}")
    private String githubClientSecret;

    @Value("${app.oauth.github.redirect-uri:http://localhost:8080/api/auth/github/callback}")
    private String githubRedirectUri;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User(
                request.getName().trim(),
                email,
                passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    public String buildGoogleAuthorizationUrl() {
        validateSocialConfig("google");
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + urlEncode(googleClientId)
                + "&redirect_uri=" + urlEncode(googleRedirectUri)
                + "&response_type=code"
                + "&scope=" + urlEncode("openid email profile")
                + "&access_type=online"
                + "&prompt=select_account";
    }

    public String buildGithubAuthorizationUrl() {
        validateSocialConfig("github");
        return "https://github.com/login/oauth/authorize"
                + "?client_id=" + urlEncode(githubClientId)
                + "&redirect_uri=" + urlEncode(githubRedirectUri)
                + "&scope=user:email";
    }

    public String handleGoogleCallback(String code) {
        String accessToken = exchangeCodeForAccessToken(
                "https://oauth2.googleapis.com/token",
                Map.of(
                        "client_id", googleClientId,
                        "client_secret", googleClientSecret,
                        "code", code,
                        "grant_type", "authorization_code",
                        "redirect_uri", googleRedirectUri
                ),
                "google"
        );

        JsonNode userInfo = fetchJson("https://openidconnect.googleapis.com/v1/userinfo", accessToken, "google");
        String name = safeText(userInfo.get("name"), "Google User");
        String email = safeText(userInfo.get("email"), "");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google account email is required for sign-in.");
        }

        return redirectWithToken(name, email);
    }

    public String handleGithubCallback(String code) {
        String accessToken = exchangeCodeForAccessToken(
                "https://github.com/login/oauth/access_token",
                Map.of(
                        "client_id", githubClientId,
                        "client_secret", githubClientSecret,
                        "code", code,
                        "redirect_uri", githubRedirectUri
                ),
                "github"
        );

        JsonNode userInfo = fetchJson("https://api.github.com/user", accessToken, "github");
        String name = safeText(userInfo.get("name"), safeText(userInfo.get("login"), "GitHub User"));
        String email = safeText(userInfo.get("email"), "");

        if (email == null || email.isBlank()) {
            JsonNode emailResponse = fetchJson("https://api.github.com/user/emails", accessToken, "github");
            if (emailResponse.isArray()) {
                for (JsonNode item : emailResponse) {
                    if (item.has("primary") && item.get("primary").asBoolean() && item.has("email")) {
                        email = item.get("email").asText();
                        break;
                    }
                }
            }
        }

        if (email == null || email.isBlank()) {
            email = name.trim().replaceAll("\\s+", "-").toLowerCase() + "@github.local";
        }

        return redirectWithToken(name, email);
    }

    private String redirectWithToken(String name, String email) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> userRepository.save(new User(
                        name == null || name.isBlank() ? "Social User" : name,
                        normalizedEmail,
                        passwordEncoder.encode(UUID.randomUUID().toString())
                )));

        String token = jwtUtil.generateToken(user.getEmail());
        return frontendUrl
                + "/login?token=" + urlEncode(token)
                + "&name=" + urlEncode(user.getName())
                + "&email=" + urlEncode(user.getEmail());
    }

    private String exchangeCodeForAccessToken(String tokenUrl, Map<String, String> formData, String provider) {
        validateSocialConfig(provider);

        try {
            String body = formData.entrySet().stream()
                    .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new IllegalStateException(provider + " provider returned an error while exchanging the code: " + response.body());
            }

            JsonNode json = objectMapper.readTree(response.body());
            String accessToken = json.get("access_token") != null ? json.get("access_token").asText() : null;
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("No access token returned from " + provider + " OAuth provider.");
            }
            return accessToken;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to contact the " + provider + " OAuth provider.", e);
        }
    }

    private JsonNode fetchJson(String url, String accessToken, String provider) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Unable to fetch user profile from " + provider + ": " + response.body());
            }

            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to read profile data from " + provider + ".", e);
        }
    }

    private void validateSocialConfig(String provider) {
        if ("google".equals(provider) && (googleClientId == null || googleClientId.isBlank() || googleClientSecret == null || googleClientSecret.isBlank())) {
            throw new IllegalStateException("Google OAuth is not configured. Set app.oauth.google.client-id and app.oauth.google.client-secret.");
        }
        if ("github".equals(provider) && (githubClientId == null || githubClientId.isBlank() || githubClientSecret == null || githubClientSecret.isBlank())) {
            throw new IllegalStateException("GitHub OAuth is not configured. Set app.oauth.github.client-id and app.oauth.github.client-secret.");
        }
    }

    private String safeText(JsonNode node, String fallback) {
        if (node == null || node.isNull() || node.asText() == null || node.asText().isBlank()) {
            return fallback;
        }
        return node.asText();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
