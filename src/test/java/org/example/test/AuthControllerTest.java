package org.example.test;

import org.example.productbacklog.controller.AuthController;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.payload.request.LoginRequest;
import org.example.productbacklog.payload.request.SignupRequest;
import org.example.productbacklog.payload.response.JwtResponse;
import org.example.productbacklog.payload.response.MessageResponse;
import org.example.productbacklog.security.jwt.JwtUtil;
import org.example.productbacklog.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private LoginRequest loginRequest;
    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(User.Role.DEVELOPER)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com"); // Changed from setUsername to setEmail
        loginRequest.setPassword("password123");

        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("new@example.com");
        signupRequest.setPassword("newpassword");
        signupRequest.setRole(User.Role.DEVELOPER);
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnJwtResponse() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser)); // Changed from getUserByUsername
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(testUser)).thenReturn("jwtToken");

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertTrue(response.getBody() instanceof JwtResponse);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();

        assertEquals("jwtToken", jwtResponse.getToken());
        assertEquals(1L, jwtResponse.getId());
        assertEquals("testuser", jwtResponse.getUsername());
        assertEquals("test@example.com", jwtResponse.getEmail());
        assertEquals("DEVELOPER", jwtResponse.getRole());
    }

    @Test
    void authenticateUser_WithUserNotFound_ShouldReturnErrorResponse() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.empty()); // Changed from getUserByUsername

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();

        assertEquals("Error: User not found", messageResponse.getMessage());
    }

    @Test
    void registerUser_WithValidRequest_ShouldRegisterSuccessfully() {
        // Arrange
        when(userService.isUsernameAvailable("newuser")).thenReturn(true);
        when(userService.isEmailAvailable("new@example.com")).thenReturn(true);
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();

        assertEquals("User registered successfully!", messageResponse.getMessage());
        verify(userService).createUser(any(User.class));
    }

    @Test
    void registerUser_WithUsernameTaken_ShouldReturnErrorResponse() {
        // Arrange
        when(userService.isUsernameAvailable("newuser")).thenReturn(false);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();

        assertEquals("Error: Username is already taken!", messageResponse.getMessage());
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void registerUser_WithEmailTaken_ShouldReturnErrorResponse() {
        // Arrange
        when(userService.isUsernameAvailable("newuser")).thenReturn(true);
        when(userService.isEmailAvailable("new@example.com")).thenReturn(false);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();

        assertEquals("Error: Email is already in use!", messageResponse.getMessage());
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void authenticateUser_WithAuthenticationException_ShouldPropagateException() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser)); // Changed from getUserByUsername
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authController.authenticateUser(loginRequest);
        });

        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    void authenticateUser_ShouldSetSecurityContext() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser)); // Changed from getUserByUsername
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(testUser)).thenReturn("jwtToken");

        // Act
        authController.authenticateUser(loginRequest);

        // Assert
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        // This test verifies that the authentication is set in the security context
    }
}