package org.example.productbacklog.service;

import org.example.productbacklog.dto.UserDTO;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO createUser(UserDTO userDTO, String password);
    UserDTO updateUser(Long userId, UserDTO userDTO);
    void deleteUser(Long userId);
    UserDTO getUserById(Long userId) throws ResourceNotFoundException;
    Optional<UserDTO> getUserByUsername(String username);
    Optional<UserDTO> getUserByEmail(String email);
    List<UserDTO> getAllUsers();
    List<UserDTO> searchUsers(String searchTerm);
    Optional<UserDTO> authenticateUser(String username, String password);
    boolean verifyPassword(String rawPassword, String encodedPassword);
    List<UserDTO> getUsersByRole(User.Role role);
    List<UserDTO> getUsersByRoles(List<User.Role> roles);
    List<UserDTO> getUsersByProjectId(Long projectId);
    long countUsersByRole(User.Role role);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);

    // New methods
    List<UserDTO> getUsersByUserStoryId(Long userStoryId);
    List<UserDTO> findUsersWithoutTasks();
    boolean hasAssignedTasks(Long userId);
}