package org.example.productbacklog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.productbacklog.dto.UserDTO;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "User object that needs to be created", required = true) @RequestBody UserDTO userDTO,
            @Parameter(description = "Password for the user", required = true) @RequestParam String password) {
        UserDTO createdUser = userService.createUser(userDTO, password);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all users")
    @ApiResponse(responseCode = "200", description = "List of all users")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true) @PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Update user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID of the user to update", required = true) @PathVariable Long id,
            @RequestBody UserDTO userData) {
        try {
            UserDTO updatedUser = userService.updateUser(id, userData);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true) @PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Search for users by username or email")
    @ApiResponse(responseCode = "200", description = "List of users matching the search term")
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @Parameter(description = "Search term for username or email", required = true) @RequestParam String searchTerm) {
        List<UserDTO> users = userService.searchUsers(searchTerm);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Authenticate user by username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @PostMapping("/authenticate")
    public ResponseEntity<UserDTO> authenticateUser(
            @Parameter(description = "Username of the user", required = true) @RequestParam String username,
            @Parameter(description = "Password of the user", required = true) @RequestParam String password) {

        return userService.authenticateUser(username, password)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Operation(summary = "Check if username is available")
    @ApiResponse(responseCode = "200", description = "Username availability status")
    @GetMapping("/username-available")
    public ResponseEntity<Boolean> isUsernameAvailable(
            @Parameter(description = "Username to check availability", required = true) @RequestParam String username) {
        boolean isAvailable = userService.isUsernameAvailable(username);
        return new ResponseEntity<>(isAvailable, HttpStatus.OK);
    }

    @Operation(summary = "Check if email is available")
    @ApiResponse(responseCode = "200", description = "Email availability status")
    @GetMapping("/email-available")
    public ResponseEntity<Boolean> isEmailAvailable(
            @Parameter(description = "Email to check availability", required = true) @RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        return new ResponseEntity<>(isAvailable, HttpStatus.OK);
    }

    @Operation(summary = "Get users by role")
    @ApiResponse(responseCode = "200", description = "List of users by role")
    @GetMapping("/role")
    public ResponseEntity<List<UserDTO>> getUsersByRole(
            @Parameter(description = "Role of the users to retrieve", required = true) @RequestParam User.Role role) {
        List<UserDTO> users = userService.getUsersByRole(role);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Get users by multiple roles")
    @ApiResponse(responseCode = "200", description = "List of users by roles")
    @GetMapping("/roles")
    public ResponseEntity<List<UserDTO>> getUsersByRoles(
            @Parameter(description = "List of roles to retrieve users for", required = true) @RequestParam List<User.Role> roles) {
        List<UserDTO> users = userService.getUsersByRoles(roles);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Get users by project ID")
    @ApiResponse(responseCode = "200", description = "List of users by project ID")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<UserDTO>> getUsersByProjectId(
            @Parameter(description = "Project ID to retrieve users associated with", required = true) @PathVariable Long projectId) {
        List<UserDTO> users = userService.getUsersByProjectId(projectId);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Count users by role")
    @ApiResponse(responseCode = "200", description = "Count of users by role")
    @GetMapping("/count-by-role")
    public ResponseEntity<Long> countUsersByRole(
            @Parameter(description = "Role to count users by", required = true) @RequestParam User.Role role) {
        long count = userService.countUsersByRole(role);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}