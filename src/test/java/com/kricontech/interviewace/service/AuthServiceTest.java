package com.kricontech.interviewace.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kricontech.interviewace.dto.AuthResponse;
import com.kricontech.interviewace.dto.LoginRequest;
import com.kricontech.interviewace.dto.RegisterRequest;
import com.kricontech.interviewace.model.User;
import com.kricontech.interviewace.repository.UserRepository;
import com.kricontech.interviewace.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerNormalizesIdentityAndStoresEncodedPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setName("  Casey Example  ");
        request.setEmail("  CASEY@EXAMPLE.COM ");
        request.setPassword("password");
        when(userRepository.existsByEmail("casey@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("casey@example.com")).thenReturn("token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("Casey Example", userCaptor.getValue().getName());
        assertEquals("casey@example.com", userCaptor.getValue().getEmail());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());
        assertEquals("token", response.getToken());
        assertEquals("Casey Example", response.getName());
        assertEquals("casey@example.com", response.getEmail());
    }

    @Test
    void registerRejectsAnExistingEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Casey");
        request.setEmail("CASEY@example.com");
        request.setPassword("password");
        when(userRepository.existsByEmail("casey@example.com")).thenReturn(true);

        assertEquals("An account with this email already exists.",
                assertThrows(IllegalArgumentException.class, () -> authService.register(request)).getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void loginNormalizesEmailAndReturnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail(" CASEY@EXAMPLE.COM ");
        request.setPassword("password");
        User user = new User("Casey", "casey@example.com", "encoded-password");
        when(userRepository.findByEmail("casey@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken("casey@example.com")).thenReturn("token");

        AuthResponse response = authService.login(request);

        assertEquals("token", response.getToken());
        assertEquals("Casey", response.getName());
        assertEquals("casey@example.com", response.getEmail());
    }

    @Test
    void loginRejectsUnknownEmailAndIncorrectPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("password");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertEquals("Invalid email or password.",
                assertThrows(IllegalArgumentException.class, () -> authService.login(request)).getMessage());

        User user = new User("Casey", "missing@example.com", "encoded-password");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(false);

        assertEquals("Invalid email or password.",
                assertThrows(IllegalArgumentException.class, () -> authService.login(request)).getMessage());
        verify(jwtUtil, never()).generateToken(any());
    }
}