package org.example.test;

import org.example.productbacklog.entity.Comment;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.exception.ResourceNotFoundException;
import org.example.productbacklog.repository.ProjectRepository;
import org.example.productbacklog.repository.UserRepository;
import org.example.productbacklog.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User testUser2;
    private User testUser3;
    private Project testProject;
    private Project testProject2;
    private Task testTask;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(User.Role.DEVELOPER)
                .build();

        testUser2 = User.builder()
                .id(2L)
                .username("productowner")
                .email("po@example.com")
                .password("password456")
                .role(User.Role.PRODUCT_OWNER)
                .build();

        testUser3 = User.builder()
                .id(3L)
                .username("scrummaster")
                .email("sm@example.com")
                .password("password789")
                .role(User.Role.SCRUM_MASTER)
                .build();

        // Create test projects
        testProject = new Project("Test Project");
        testProject.setProjectId(1);
        testProject.setUser(testUser);

        testProject2 = new Project("Another Project");
        testProject2.setProjectId(2);
        testProject2.setUser(testUser2);

        // Create test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setAssignedUser(testUser);

        // Create test comment
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setUser(testUser);

        // Set up relationships
        List<Project> projects = new ArrayList<>();
        projects.add(testProject);
        testUser.setProjects(projects);

        List<Task> tasks = new ArrayList<>();
        tasks.add(testTask);
        testUser.setTasks(tasks);

        List<Comment> comments = new ArrayList<>();
        comments.add(testComment);
        testUser.setComments(comments);
    }

    // Original tests here...

    @Test
    void createUser_ShouldEncodePasswordAndSaveUser() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(testUser);

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(testUser);
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void createUser_ShouldHandleEmptyCollections() {
        // Arrange
        User userWithNullCollections = User.builder()
                .id(4L)
                .username("empty")
                .email("empty@example.com")
                .password("password")
                .role(User.Role.DEVELOPER)
                .projects(null)
                .tasks(null)
                .comments(null)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userWithNullCollections);

        // Act
        User result = userService.createUser(userWithNullCollections);

        // Assert
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(userWithNullCollections);
        assertThat(result).isEqualTo(userWithNullCollections);
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        // Arrange
        User updatedUser = User.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("newpassword")
                .role(User.Role.SCRUM_MASTER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updatedUser);

        // Assert
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(testUser);

        assertEquals("updateduser", testUser.getUsername());
        assertEquals("updated@example.com", testUser.getEmail());
        assertEquals(User.Role.SCRUM_MASTER, testUser.getRole());
    }

    @Test
    void updateUser_ShouldNotUpdatePasswordWhenEmpty() {
        // Arrange
        User updatedUserNoPassword = User.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("")  // Empty password should not trigger update
                .role(User.Role.SCRUM_MASTER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updatedUserNoPassword);

        // Assert
        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());  // Password encoder should not be called
        verify(userRepository).save(testUser);

        assertEquals("updateduser", testUser.getUsername());
        assertEquals("updated@example.com", testUser.getEmail());
        assertEquals("password123", testUser.getPassword());  // Password should remain unchanged
    }

    @Test
    void updateUser_ShouldNotUpdatePasswordWhenNull() {
        // Arrange
        User updatedUserNullPassword = User.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password(null)  // Null password should not trigger update
                .role(User.Role.SCRUM_MASTER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updatedUserNullPassword);

        // Assert
        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());  // Password encoder should not be called
        verify(userRepository).save(testUser);

        assertEquals("updateduser", testUser.getUsername());
        assertEquals("updated@example.com", testUser.getEmail());
        assertEquals("password123", testUser.getPassword());  // Password should remain unchanged
    }

    @Test
    void updateUser_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.updateUser(99L, testUser)
        );

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteExistingUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.deleteUser(99L)
        );

        verify(userRepository).findById(99L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUserWhenFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        verify(userRepository).findById(1L);
        assertEquals(testUser, result);
    }

    @Test
    void getUserById_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserById(99L)
        );

        verify(userRepository).findById(99L);
    }

    @Test
    void getUserByUsername_ShouldReturnUserWhenFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserByUsername("testuser");

        // Assert
        verify(userRepository).findByUsername("testuser");
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void getUserByUsername_ShouldReturnEmptyWhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserByUsername("nonexistent");

        // Assert
        verify(userRepository).findByUsername("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserByUsername_ShouldHandleCaseSensitivity() {
        // Arrange
        when(userRepository.findByUsername("TESTUSER")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> resultUpper = userService.getUserByUsername("TESTUSER");
        Optional<User> resultLower = userService.getUserByUsername("testuser");

        // Assert
        verify(userRepository).findByUsername("TESTUSER");
        verify(userRepository).findByUsername("testuser");
        assertTrue(resultUpper.isEmpty());
        assertTrue(resultLower.isPresent());
    }

    @Test
    void getUserByEmail_ShouldReturnUserWhenFound() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserByEmail("test@example.com");

        // Assert
        verify(userRepository).findByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void getUserByEmail_ShouldReturnEmptyWhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserByEmail("nonexistent@example.com");

        // Assert
        verify(userRepository).findByEmail("nonexistent@example.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUsers_ShouldReturnEmptyListWhenNoUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        verify(userRepository).findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser, testUser2, testUser3);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        verify(userRepository).findAll();
        assertEquals(3, result.size());
        assertTrue(result.contains(testUser));
        assertTrue(result.contains(testUser2));
        assertTrue(result.contains(testUser3));
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser, testUser2);
        when(userRepository.findByUsernameContainingOrEmailContaining("test", "test"))
                .thenReturn(users);

        // Act
        List<User> result = userService.searchUsers("test");

        // Assert
        verify(userRepository).findByUsernameContainingOrEmailContaining("test", "test");
        assertEquals(2, result.size());
    }

    @Test
    void searchUsers_ShouldReturnEmptyListWhenNoMatches() {
        // Arrange
        when(userRepository.findByUsernameContainingOrEmailContaining("xyz", "xyz"))
                .thenReturn(Collections.emptyList());

        // Act
        List<User> result = userService.searchUsers("xyz");

        // Assert
        verify(userRepository).findByUsernameContainingOrEmailContaining("xyz", "xyz");
        assertTrue(result.isEmpty());
    }

    @Test
    void searchUsers_ShouldHandleEmptySearchTerm() {
        // Arrange
        List<User> allUsers = Arrays.asList(testUser, testUser2, testUser3);
        when(userRepository.findByUsernameContainingOrEmailContaining("", ""))
                .thenReturn(allUsers);

        // Act
        List<User> result = userService.searchUsers("");

        // Assert
        verify(userRepository).findByUsernameContainingOrEmailContaining("", "");
        assertEquals(3, result.size());
    }

    @Test
    void authenticateUser_ShouldReturnUserWhenCredentialsAreValid() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "password123")).thenReturn(true);

        // Act
        Optional<User> result = userService.authenticateUser("testuser", "password123");

        // Assert
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "password123");
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void authenticateUser_ShouldReturnEmptyWhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.authenticateUser("nonexistent", "password123");

        // Assert
        verify(userRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        assertTrue(result.isEmpty());
    }

    @Test
    void authenticateUser_ShouldReturnEmptyWhenPasswordIsInvalid() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "password123")).thenReturn(false);

        // Act
        Optional<User> result = userService.authenticateUser("testuser", "wrongpassword");

        // Assert
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", "password123");
        assertTrue(result.isEmpty());
    }

    @Test
    void verifyPassword_ShouldReturnTrueWhenPasswordMatches() {
        // Arrange
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // Act
        boolean result = userService.verifyPassword("password123", "encodedPassword");

        // Assert
        verify(passwordEncoder).matches("password123", "encodedPassword");
        assertTrue(result);
    }

    @Test
    void verifyPassword_ShouldReturnFalseWhenPasswordDoesNotMatch() {
        // Arrange
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act
        boolean result = userService.verifyPassword("wrongpassword", "encodedPassword");

        // Assert
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
        assertFalse(result);
    }

    @Test
    void getUsersByRole_ShouldReturnUsersWithSpecificRole() {
        // Arrange
        List<User> developers = List.of(testUser);
        when(userRepository.findByRole(User.Role.DEVELOPER)).thenReturn(developers);

        // Act
        List<User> result = userService.getUsersByRole(User.Role.DEVELOPER);

        // Assert
        verify(userRepository).findByRole(User.Role.DEVELOPER);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
    }

    @Test
    void getUsersByRole_ShouldReturnEmptyListWhenNoUsersWithRole() {
        // Arrange
        when(userRepository.findByRole(User.Role.TESTER)).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userService.getUsersByRole(User.Role.TESTER);

        // Assert
        verify(userRepository).findByRole(User.Role.TESTER);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByRoles_ShouldReturnUsersWithSpecificRoles() {
        // Arrange
        List<User> users = Arrays.asList(testUser, testUser2);
        List<User.Role> roles = Arrays.asList(User.Role.DEVELOPER, User.Role.PRODUCT_OWNER);

        when(userRepository.findByRoleIn(roles)).thenReturn(users);

        // Act
        List<User> result = userService.getUsersByRoles(roles);

        // Assert
        verify(userRepository).findByRoleIn(roles);
        assertEquals(2, result.size());
    }

    @Test
    void getUsersByRoles_ShouldReturnEmptyListWhenNoMatchingRoles() {
        // Arrange
        List<User.Role> roles = Arrays.asList(User.Role.TESTER, User.Role.ADMIN);
        when(userRepository.findByRoleIn(roles)).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userService.getUsersByRoles(roles);

        // Assert
        verify(userRepository).findByRoleIn(roles);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByRoles_ShouldHandleEmptyRolesList() {
        // Arrange
        List<User.Role> emptyRoles = Collections.emptyList();
        when(userRepository.findByRoleIn(emptyRoles)).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userService.getUsersByRoles(emptyRoles);

        // Assert
        verify(userRepository).findByRoleIn(emptyRoles);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByProjectId_ShouldReturnUsersAssociatedWithProject() {
        // Arrange
        when(projectRepository.findById(1)).thenReturn(Optional.of(testProject));
        when(userRepository.findUsersByProjectId(1)).thenReturn(List.of(testUser));

        // Act
        List<User> result = userService.getUsersByProjectId(1L);

        // Assert
        verify(projectRepository).findById(1);
        verify(userRepository).findUsersByProjectId(1);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
    }

    @Test
    void getUsersByProjectId_ShouldReturnEmptyListWhenProjectNotFound() {
        // Arrange
        when(projectRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        List<User> result = userService.getUsersByProjectId(99L);

        // Assert
        verify(projectRepository).findById(99);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByProjectId_ShouldReturnEmptyListWhenProjectHasNoUser() {
        // Arrange
        Project projectWithoutUser = new Project("Orphan Project");
        projectWithoutUser.setProjectId(3);
        projectWithoutUser.setUser(null);

        when(projectRepository.findById(3)).thenReturn(Optional.of(projectWithoutUser));

        // Act
        List<User> result = userService.getUsersByProjectId(3L);

        // Assert
        verify(projectRepository).findById(3);
        assertTrue(result.isEmpty());
    }

    @Test
    void countUsersByRole_ShouldReturnUserCount() {
        // Arrange
        when(userRepository.countByRole(User.Role.DEVELOPER)).thenReturn(5L);

        // Act
        long result = userService.countUsersByRole(User.Role.DEVELOPER);

        // Assert
        verify(userRepository).countByRole(User.Role.DEVELOPER);
        assertEquals(5L, result);
    }

    @Test
    void countUsersByRole_ShouldReturnZeroWhenNoUsersWithRole() {
        // Arrange
        when(userRepository.countByRole(User.Role.ADMIN)).thenReturn(0L);

        // Act
        long result = userService.countUsersByRole(User.Role.ADMIN);

        // Assert
        verify(userRepository).countByRole(User.Role.ADMIN);
        assertEquals(0L, result);
    }

    @Test
    void isUsernameAvailable_ShouldReturnTrueWhenUsernameIsAvailable() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        // Act
        boolean result = userService.isUsernameAvailable("newuser");

        // Assert
        verify(userRepository).existsByUsername("newuser");
        assertTrue(result);
    }

    @Test
    void isUsernameAvailable_ShouldReturnFalseWhenUsernameIsNotAvailable() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = userService.isUsernameAvailable("testuser");

        // Assert
        verify(userRepository).existsByUsername("testuser");
        assertFalse(result);
    }

    @Test
    void isUsernameAvailable_ShouldHandleEmptyUsername() {
        // Arrange
        when(userRepository.existsByUsername("")).thenReturn(false);

        // Act
        boolean result = userService.isUsernameAvailable("");

        // Assert
        verify(userRepository).existsByUsername("");
        assertTrue(result);
    }

    @Test
    void isEmailAvailable_ShouldReturnTrueWhenEmailIsAvailable() {
        // Arrange
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // Act
        boolean result = userService.isEmailAvailable("new@example.com");

        // Assert
        verify(userRepository).existsByEmail("new@example.com");
        assertTrue(result);
    }

    @Test
    void isEmailAvailable_ShouldReturnFalseWhenEmailIsNotAvailable() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userService.isEmailAvailable("test@example.com");

        // Assert
        verify(userRepository).existsByEmail("test@example.com");
        assertFalse(result);
    }

    @Test
    void isEmailAvailable_ShouldHandleEmptyEmail() {
        // Arrange
        when(userRepository.existsByEmail("")).thenReturn(false);

        // Act
        boolean result = userService.isEmailAvailable("");

        // Assert
        verify(userRepository).existsByEmail("");
        assertTrue(result);
    }

    @Test
    void isEmailAvailable_ShouldBeCaseInsensitive() {
        // This test assumes that your repository implementation treats emails as case insensitive
        // Arrange
        when(userRepository.existsByEmail("TEST@example.com")).thenReturn(true);

        // Act
        boolean result = userService.isEmailAvailable("TEST@example.com");

        // Assert
        verify(userRepository).existsByEmail("TEST@example.com");
        assertFalse(result);
    }
}