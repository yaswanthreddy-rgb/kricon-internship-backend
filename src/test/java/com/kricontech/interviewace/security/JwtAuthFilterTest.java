package com.kricontech.interviewace.security;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kricontech.interviewace.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private UserRepository userRepository;

    private JwtUtil jwtUtil;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtUtil = new JwtUtil("filter-test-secret-with-sufficient-key-material", 60_000);
        filter = new JwtAuthFilter(jwtUtil, userRepository);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesBearerTokenForExistingUser() throws ServletException, IOException {
        String email = "casey@example.com";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtUtil.generateToken(email));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(email, authentication.getPrincipal());
        verify(chain).doFilter(request, response);
    }

    @Test
    void leavesRequestUnauthenticatedWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).existsByEmail(org.mockito.ArgumentMatchers.anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void leavesRequestUnauthenticatedForInvalidTokenOrDeletedUser() throws ServletException, IOException {
        MockHttpServletRequest invalidRequest = new MockHttpServletRequest();
        invalidRequest.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse invalidResponse = new MockHttpServletResponse();
        FilterChain invalidChain = mock(FilterChain.class);

        filter.doFilter(invalidRequest, invalidResponse, invalidChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).existsByEmail(org.mockito.ArgumentMatchers.anyString());

        SecurityContextHolder.clearContext();
        String deletedEmail = "deleted@example.com";
        MockHttpServletRequest deletedUserRequest = new MockHttpServletRequest();
        deletedUserRequest.addHeader("Authorization", "Bearer " + jwtUtil.generateToken(deletedEmail));
        MockHttpServletResponse deletedUserResponse = new MockHttpServletResponse();
        FilterChain deletedUserChain = mock(FilterChain.class);
        when(userRepository.existsByEmail(deletedEmail)).thenReturn(false);

        filter.doFilter(deletedUserRequest, deletedUserResponse, deletedUserChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(deletedUserChain).doFilter(deletedUserRequest, deletedUserResponse);
    }
}