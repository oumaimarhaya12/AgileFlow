package org.example.productbacklog.service.impl;

import org.example.productbacklog.converter.UserConverter;
import org.example.productbacklog.dto.UserDTO;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.exception.ResourceNotFoundException;
import org.example.productbacklog.repository.ProjectRepository;
import org.example.productbacklog.repository.UserRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private final UserStoryRepository userStoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserConverter userConverter;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           UserStoryRepository userStoryRepository,
                           @Lazy PasswordEncoder passwordEncoder, // Add @Lazy here
                           UserConverter userConverter) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.userStoryRepository = userStoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.userConverter = userConverter;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO, String password) {
        // Convert DTO to entity
        User user = userConverter.convertToEntity(userDTO);

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(password));

        // Save the user
        User savedUser = userRepository.save(user);

        // Convert back to DTO and return
        return userConverter.convertToDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update user fields
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setRole(userDTO.getRole());

        // Save the updated user
        User updatedUser = userRepository.save(existingUser);

        // Convert to DTO and return
        return userConverter.convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    @Override
    public UserDTO getUserById(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userConverter.convertToDTO(user);
    }

    @Override
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userConverter::convertToDTO);
    }

    @Override
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userConverter::convertToDTO);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userConverter.convertToDTOList(users);
    }

    @Override
    public List<UserDTO> searchUsers(String searchTerm) {
        List<User> users = userRepository.findByUsernameContainingOrEmailContaining(searchTerm, searchTerm);
        return userConverter.convertToDTOList(users);
    }

    @Override
    public Optional<UserDTO> authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent() && verifyPassword(password, userOpt.get().getPassword())) {
            return userOpt.map(userConverter::convertToDTO);
        }

        return Optional.empty();
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public List<UserDTO> getUsersByRole(User.Role role) {
        List<User> users = userRepository.findByRole(role);
        return userConverter.convertToDTOList(users);
    }

    @Override
    public List<UserDTO> getUsersByRoles(List<User.Role> roles) {
        List<User> users = userRepository.findByRoleIn(roles);
        return userConverter.convertToDTOList(users);
    }

    @Override
    public List<UserDTO> getUsersByProjectId(Long projectId) {
        // First check if the project exists
        Optional<Project> projectOpt = projectRepository.findById(projectId.intValue());

        if (projectOpt.isPresent()) {
            // Return all users associated with this project ID from the repository
            List<User> users = userRepository.findUsersByProjectId(projectId.intValue());
            return userConverter.convertToDTOList(users);
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

    // New methods implementation

    @Override
    public List<UserDTO> getUsersByUserStoryId(Long userStoryId) {
        // First check if the user story exists
        Optional<UserStory> userStoryOpt = userStoryRepository.findById(userStoryId);

        if (userStoryOpt.isPresent()) {
            // Return all users associated with this user story ID from the repository
            List<User> users = userRepository.findUsersByUserStoryId(userStoryId);
            return userConverter.convertToDTOList(users);
        }

        // Return empty list if user story not found
        return Collections.emptyList();
    }

    @Override
    public List<UserDTO> findUsersWithoutTasks() {
        List<User> users = userRepository.findUsersWithoutTasks();
        return userConverter.convertToDTOList(users);
    }

    @Override
    public boolean hasAssignedTasks(Long userId) {
        return userRepository.hasAssignedTasks(userId);
    }
}