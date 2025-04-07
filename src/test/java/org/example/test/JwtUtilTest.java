package org.example.test;

import org.example.productbacklog.dto.UserDTO;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.security.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;
    private UserDTO testUserDTO;
    private static final String SECRET = "testSecret123456789012345678901234567890";
    private static final long EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);

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
    }

    @Test
    void generateToken_WithEntity_ShouldCreateValidToken() {
        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void generateToken_WithDTO_ShouldCreateValidToken() {
        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void extractExpiration_ShouldReturnFutureDate() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");

        // Act
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForInvalidUsername() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("wronguser");

        // Act
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldHandleExpiredToken() throws Exception {
        // Arrange - create a token with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "expiration", 1); // 1ms expiration

        String token = shortExpirationJwtUtil.generateToken(testUser);

        // Wait for token to expire
        Thread.sleep(10);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");

        // Act & Assert
        assertFalse(shortExpirationJwtUtil.validateToken(token, userDetails));
    }

    @Test
    void validateToken_ShouldHandleInvalidToken() {
        // Arrange
        String invalidToken = "invalidToken";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");

        // Act & Assert
        assertFalse(jwtUtil.validateToken(invalidToken, userDetails));
    }
}