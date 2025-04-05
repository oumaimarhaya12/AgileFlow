package org.example.test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.security.jwt.JwtAuthenticationFilter;
import org.example.productbacklog.security.jwt.JwtUtil;
import org.example.productbacklog.security.jwt.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TestableJwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;
    private String validToken;

    // Create a testable subclass that exposes the protected method
    static class TestableJwtAuthenticationFilter extends JwtAuthenticationFilter {
        public void doFilterInternalForTest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            super.doFilterInternal(request, response, filterChain);
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();

        // Create a valid user details
        userDetails = new org.springframework.security.core.userdetails.User(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        validToken = "valid.jwt.token";
    }

    @Test
    void testDoFilterInternal_WithValidToken() throws ServletException, IOException {
        // Setup
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, userDetails)).thenReturn(true);

        // Execute
        jwtAuthenticationFilter.doFilterInternalForTest(request, response, filterChain);

        // Verify
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_WithNoAuthorizationHeader() throws ServletException, IOException {
        // Setup
        when(request.getHeader("Authorization")).thenReturn(null);

        // Execute
        jwtAuthenticationFilter.doFilterInternalForTest(request, response, filterChain);

        // Verify
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_WithInvalidAuthorizationHeader() throws ServletException, IOException {
        // Setup
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        // Execute
        jwtAuthenticationFilter.doFilterInternalForTest(request, response, filterChain);

        // Verify
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_WithInvalidToken() throws ServletException, IOException {
        // Setup
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, userDetails)).thenReturn(false);

        // Execute
        jwtAuthenticationFilter.doFilterInternalForTest(request, response, filterChain);

        // Verify
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_WithException() throws ServletException, IOException {
        // Setup
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("Test exception"));

        // Execute
        jwtAuthenticationFilter.doFilterInternalForTest(request, response, filterChain);

        // Verify
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}