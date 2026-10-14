package com.attendance.backend;

import com.attendance.backend.dto.request.LoginRequest;
import com.attendance.backend.dto.request.RegisterRequest;
import com.attendance.backend.dto.response.AuthResponse;
import com.attendance.backend.entity.Role;
import com.attendance.backend.entity.User;
import com.attendance.backend.exception.EmailAlreadyExistsException;
import com.attendance.backend.exception.InvalidCredentialsException;
import com.attendance.backend.repository.UserRepository;
import com.attendance.backend.security.JwtUtils;
import com.attendance.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("John Teacher", "teacher@example.com", "encoded_pass", Role.TEACHER);
        sampleUser.setId(1L);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("John Teacher", "teacher@example.com", "password123", Role.TEACHER);

        when(userRepository.existsByEmail("teacher@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtUtils.generateToken(any(User.class))).thenReturn("mock_jwt_token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock_jwt_token", response.getToken());
        assertEquals("teacher@example.com", response.getUser().getEmail());
        assertEquals(Role.TEACHER, response.getUser().getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmailThrowsException() {
        RegisterRequest request = new RegisterRequest("John Teacher", "teacher@example.com", "password123", Role.TEACHER);

        when(userRepository.existsByEmail("teacher@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("teacher@example.com", "password123");

        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "encoded_pass")).thenReturn(true);
        when(jwtUtils.generateToken(sampleUser)).thenReturn("mock_jwt_token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock_jwt_token", response.getToken());
        assertEquals(1L, response.getUser().getId());
    }

    @Test
    void testLoginInvalidPasswordThrowsException() {
        LoginRequest request = new LoginRequest("teacher@example.com", "wrong_password");

        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrong_password", "encoded_pass")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void testLoginNonExistentEmailThrowsException() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}
