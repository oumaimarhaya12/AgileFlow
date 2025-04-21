package org.example.productbacklog.security;

import org.example.productbacklog.security.jwt.JwtAuthenticationEntryPoint;
import org.example.productbacklog.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    @Lazy
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // Allow user registration
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()

                        // Admin privileges
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "PRODUCT_OWNER", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                        // Project management - Product Owner and Admin
                        .requestMatchers(HttpMethod.POST, "/api/projects/**").hasAnyRole("ADMIN", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/projects/**").hasAnyRole("ADMIN", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasAnyRole("ADMIN", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/projects/**").authenticated()

                        // Product Backlog - Product Owner mainly
                        .requestMatchers(HttpMethod.POST, "/api/productbacklogs/**").hasAnyRole("ADMIN", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/productbacklogs/**").hasAnyRole("ADMIN", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/productbacklogs/**").hasAnyRole("ADMIN", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/productbacklogs/**").authenticated()

                        // User Stories - Product Owner mainly, but readable by team
                        .requestMatchers(HttpMethod.POST, "/api/userstories/**").hasAnyRole("ADMIN", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/userstories/**").hasAnyRole("ADMIN", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/userstories/**").hasAnyRole("ADMIN", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/userstories/**").authenticated()

                        // Sprint Management - Scrum Master
                        .requestMatchers(HttpMethod.POST, "/api/sprints/**").hasAnyRole("ADMIN", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.PUT, "/api/sprints/**").hasAnyRole("ADMIN", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.DELETE, "/api/sprints/**").hasAnyRole("ADMIN", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.GET, "/api/sprints/**").authenticated()

                        // Sprint Backlog - Scrum Master
                        .requestMatchers(HttpMethod.POST, "/api/sprint-backlogs/**").hasAnyRole("ADMIN", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.PUT, "/api/sprint-backlogs/**").hasAnyRole("ADMIN", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.DELETE, "/api/sprint-backlogs/**").hasAnyRole("ADMIN", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.GET, "/api/sprint-backlogs/**").authenticated()

                        // Tasks - Developers and Scrum Master
                        .requestMatchers(HttpMethod.POST, "/api/tasks/**").hasAnyRole("ADMIN", "SCRUM_MASTER", "DEVELOPER")
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/**").hasAnyRole("ADMIN", "SCRUM_MASTER", "DEVELOPER")
                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasAnyRole("ADMIN", "SCRUM_MASTER", "DEVELOPER")
                        .requestMatchers(HttpMethod.GET, "/api/tasks/**").authenticated()

                        // Any other request needs authentication
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}