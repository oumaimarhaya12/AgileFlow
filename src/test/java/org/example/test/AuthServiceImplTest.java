package org.example.test;

import org.example.productbacklog.converter.UserConverter;
import org.example.productbacklog.dto.UserDTO;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.payload.request.LoginRequest;
import org.example.productbacklog.payload.request.SignupRequest;
import org.example.productbacklog.payload.response.JwtResponse;
import org.example.productbacklog.payload.response.MessageResponse;
import org.example.productbacklog.security.jwt.JwtUtil;
import org.example.productbacklog.service.UserService;
import org.example.productbacklog.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserConverter userConverter;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserDTO testUserDTO;
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

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setRole(User.Role.DEVELOPER);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("new@example.com");
        signupRequest.setPassword("newpassword");
        signupRequest.setRole(User.Role.DEVELOPER);

        when(userConverter.convertToEntity(testUserDTO)).thenReturn(testUser);
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnJwtResponse() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUserDTO));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(testUser)).thenReturn("jwtToken");

        ResponseEntity<?> response = authService.authenticateUser(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
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
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authService.authenticateUser(loginRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();

        assertEquals("Error: User not found", messageResponse.getMessage());
    }

    @Test
    void authenticateUser_WithTokenGenerationFailure_ShouldReturnErrorResponse() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUserDTO));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(testUser)).thenReturn("");

        ResponseEntity<?> response = authService.authenticateUser(loginRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();

        assertEquals("Error: Token generation failed", messageResponse.getMessage());
    }

    @Test
    void registerUser_WithValidRequest_ShouldRegisterSuccessfully() {
        when(userService.isUsernameAvailable("newuser")).thenReturn(true);
        when(userService.isEmailAvailable("new@example.com")).thenReturn(true);

        ResponseEntity<?> response = authService.registerUser(signupRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();

        assertEquals("User registered successfully!", messageResponse.getMessage());
        verify(userService).createUser(any(UserDTO.class), eq("newpassword"));
    }

    @Test
    void registerUser_WithUsernameTaken_ShouldReturnErrorResponse() {
        when(userService.isUsernameAvailable("newuser")).thenReturn(false);

        ResponseEntity<?> response = authService.registerUser(signupRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();

        assertEquals("Error: Username is already taken!", messageResponse.getMessage());
        verify(userService, never()).createUser(any(UserDTO.class), anyString());
    }

    @Test
    void registerUser_WithEmailTaken_ShouldReturnErrorResponse() {
        when(userService.isUsernameAvailable("newuser")).thenReturn(true);
        when(userService.isEmailAvailable("new@example.com")).thenReturn(false);

        ResponseEntity<?> response = authService.registerUser(signupRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();

        assertEquals("Error: Email is already in use!", messageResponse.getMessage());
        verify(userService, never()).createUser(any(UserDTO.class), anyString());
    }

    @Test
    void authenticateUser_WithAuthenticationException_ShouldPropagateException() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUserDTO));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        verify(jwtUtil, never()).generateToken(any(User.class));
    }
}
