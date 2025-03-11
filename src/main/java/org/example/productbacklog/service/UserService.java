package org.example.productbacklog.service;

import org.example.productbacklog.entity.User;
import org.example.productbacklog.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    User updateUser(Long userId, User userData);
    void deleteUser(Long userId);
    User getUserById(Long userId) throws ResourceNotFoundException;
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();
    List<User> searchUsers(String searchTerm);
    Optional<User> authenticateUser(String username, String password);
    boolean verifyPassword(String rawPassword, String encodedPassword);
    List<User> getUsersByRole(User.Role role);
    List<User> getUsersByRoles(List<User.Role> roles);
    List<User> getUsersByProjectId(Long projectId);
    long countUsersByRole(User.Role role);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
}