package org.example.productbacklog.service.impl;

import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.exception.ResourceNotFoundException;
import org.example.productbacklog.repository.ProjectRepository;
import org.example.productbacklog.repository.UserRepository;
import org.example.productbacklog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long userId, User userData) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update user fields
        existingUser.setUsername(userData.getUsername());
        existingUser.setEmail(userData.getEmail());

        // Only update password if provided
        if (userData.getPassword() != null && !userData.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userData.getPassword()));
        }

        existingUser.setRole(userData.getRole());

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    @Override
    public User getUserById(Long userId) throws ResourceNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByUsernameContainingOrEmailContaining(searchTerm, searchTerm);
    }

    @Override
    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent() && verifyPassword(password, userOpt.get().getPassword())) {
            return userOpt;
        }

        return Optional.empty();
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> getUsersByRoles(List<User.Role> roles) {
        return userRepository.findByRoleIn(roles);
    }

    @Override
    public List<User> getUsersByProjectId(Long projectId) {
        // First check if the project exists
        Optional<Project> projectOpt = projectRepository.findById(projectId.intValue());

        if (projectOpt.isPresent()) {
            // Return all users associated with this project ID from the repository
            return userRepository.findUsersByProjectId(projectId.intValue());
        }

        // Return empty list if project not found
        return Collections.emptyList();
    }

    @Override
    public long countUsersByRole(User.Role role) {
        return userRepository.countByRole(role);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}