package org.example.productbacklog.controller;

import org.example.productbacklog.converter.UserConverter;
import org.example.productbacklog.dto.UserDTO;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.payload.request.LoginRequest;
import org.example.productbacklog.payload.request.SignupRequest;
import org.example.productbacklog.payload.response.JwtResponse;
import org.example.productbacklog.payload.response.MessageResponse;
import org.example.productbacklog.security.jwt.JwtUtil;
import org.example.productbacklog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserConverter userConverter;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Get user by email first
        Optional<UserDTO> userDTOOpt = userService.getUserByEmail(loginRequest.getEmail());

        if (!userDTOOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found"));
        }

        UserDTO userDTO = userDTOOpt.get();

        // Convert DTO to entity for authentication and JWT generation
        User user = userConverter.convertToEntity(userDTO);

        // Authenticate with username and password
        // Spring Security still needs username for authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDTO.getId(),
                userDTO.getUsername(),
                userDTO.getEmail(),
                userDTO.getRole().name()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // Check if username is already taken
        if (!userService.isUsernameAvailable(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // Check if email is already in use
        if (!userService.isEmailAvailable(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create UserDTO from signup request
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(signUpRequest.getUsername());
        userDTO.setEmail(signUpRequest.getEmail());
        userDTO.setRole(signUpRequest.getRole());

        // Create user with DTO and password
        userService.createUser(userDTO, signUpRequest.getPassword());

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}