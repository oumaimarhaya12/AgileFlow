package org.example.test;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.example.productbacklog.converter.UserConverter;
import org.example.productbacklog.dto.UserDTO;
import org.example.productbacklog.entity.Comment;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.exception.ResourceNotFoundException;
import org.example.productbacklog.repository.ProjectRepository;
import org.example.productbacklog.repository.UserRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Spy
    private UserConverter userConverter;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User testUser2;
    private User testUser3;
    private UserDTO testUserDTO;
    private UserDTO testUserDTO2;
    private UserDTO testUserDTO3;
    private Project testProject;
    private Project testProject2;
    private Task testTask;
    private Comment testComment;
    private UserStory testUserStory;

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

        // Create test user story
        testUserStory = new UserStory();
        testUserStory.setId(1L);
        testUserStory.setTitle("Test User Story");

        // Create test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setAssignedUser(testUser);
        testTask.setUserStory(testUserStory);

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

        // Create test DTOs
        testUserDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(User.Role.DEVELOPER)
                .projectIds(List.of(1L))
                .taskIds(List.of(1L))
                .commentIds(List.of(1L))
                .build();

        testUserDTO2 = UserDTO.builder()
                .id(2L)
                .username("productowner")
                .email("po@example.com")
                .role(User.Role.PRODUCT_OWNER)
                .projectIds(List.of(2L))
                .build();

        testUserDTO3 = UserDTO.builder()
                .id(3L)
                .username("scrummaster")
                .email("sm@example.com")
                .role(User.Role.SCRUM_MASTER)
                .build();

        // Setup the spy converter with lenient() to avoid UnnecessaryStubbingException
        lenient().doReturn(testUserDTO).when(userConverter).convertToDTO(testUser);
        lenient().doReturn(testUserDTO2).when(userConverter).convertToDTO(testUser2);
        lenient().doReturn(testUserDTO3).when(userConverter).convertToDTO(testUser3);
        lenient().doReturn(testUser).when(userConverter).convertToEntity(testUserDTO);
        lenient().doReturn(testUser2).when(userConverter).convertToEntity(testUserDTO2);
        lenient().doReturn(testUser3).when(userConverter).convertToEntity(testUserDTO3);

        List<UserDTO> dtoList = Arrays.asList(testUserDTO, testUserDTO2, testUserDTO3);
        lenient().doReturn(dtoList).when(userConverter).convertToDTOList(anyList());
    }

    // Existing tests...

    @Test
    void createUser_ShouldEncodePasswordAndSaveUser() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = userService.createUser(testUserDTO, "password123");

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        assertThat(result).isEqualTo(testUserDTO);
    }

    @Test
    void createUser_ShouldHandleEmptyCollections() {
        // Arrange
        UserDTO userDTOWithEmptyCollections = UserDTO.builder()
                .id(4L)
                .username("empty")
                .email("empty@example.com")
                .role(User.Role.DEVELOPER)
                .build();

        User userWithEmptyCollections = User.builder()
                .id(4L)
                .username("empty")
                .email("empty@example.com")
                .password("encodedPassword")
                .role(User.Role.DEVELOPER)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userWithEmptyCollections);
        when(userConverter.convertToEntity(userDTOWithEmptyCollections)).thenReturn(userWithEmptyCollections);
        when(userConverter.convertToDTO(userWithEmptyCollections)).thenReturn(userDTOWithEmptyCollections);

        // Act
        UserDTO result = userService.createUser(userDTOWithEmptyCollections, "password");

        // Assert
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        assertThat(result).isEqualTo(userDTOWithEmptyCollections);
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        // Arrange
        UserDTO updatedUserDTO = UserDTO.builder()
                .username("updateduser")
                .email("updated@example.com")
                .role(User.Role.SCRUM_MASTER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO updatedUserDTOResult = UserDTO.builder()
                .id(1L)
                .username("updateduser")
                .email("updated@example.com")
                .role(User.Role.SCRUM_MASTER)
                .projectIds(List.of(1L))
                .taskIds(List.of(1L))
                .commentIds(List.of(1L))
                .build();

        doReturn(updatedUserDTOResult).when(userConverter).convertToDTO(any(User.class));

        // Act
        UserDTO result = userService.updateUser(1L, updatedUserDTO);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);

        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals(User.Role.SCRUM_MASTER, result.getRole());
    }

    @Test
    void updateUser_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.updateUser(99L, testUserDTO)
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
        UserDTO result = userService.getUserById(1L);

        // Assert
        verify(userRepository).findById(1L);
        assertEquals(testUserDTO, result);
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
        Optional<UserDTO> result = userService.getUserByUsername("testuser");

        // Assert
        verify(userRepository).findByUsername("testuser");
        assertTrue(result.isPresent());
        assertEquals(testUserDTO, result.get());
    }

    @Test
    void getUserByUsername_ShouldReturnEmptyWhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.getUserByUsername("nonexistent");

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
        Optional<UserDTO> resultUpper = userService.getUserByUsername("TESTUSER");
        Optional<UserDTO> resultLower = userService.getUserByUsername("testuser");

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
        Optional<UserDTO> result = userService.getUserByEmail("test@example.com");

        // Assert
        verify(userRepository).findByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals(testUserDTO, result.get());
    }

    @Test
    void getUserByEmail_ShouldReturnEmptyWhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.getUserByEmail("nonexistent@example.com");

        // Assert
        verify(userRepository).findByEmail("nonexistent@example.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUsers_ShouldReturnEmptyListWhenNoUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(userConverter.convertToDTOList(anyList())).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        verify(userRepository).findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser, testUser2, testUser3);
        List<UserDTO> userDTOs = Arrays.asList(testUserDTO, testUserDTO2, testUserDTO3);
        when(userRepository.findAll()).thenReturn(users);
        when(userConverter.convertToDTOList(users)).thenReturn(userDTOs);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        verify(userRepository).findAll();
        assertEquals(3, result.size());
        assertTrue(result.contains(testUserDTO));
        assertTrue(result.contains(testUserDTO2));
        assertTrue(result.contains(testUserDTO3));
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser, testUser2);
        List<UserDTO> userDTOs = Arrays.asList(testUserDTO, testUserDTO2);
        when(userRepository.findByUsernameContainingOrEmailContaining("test", "test"))
                .thenReturn(users);
        when(userConverter.convertToDTOList(users)).thenReturn(userDTOs);

        // Act
        List<UserDTO> result = userService.searchUsers("test");

        // Assert
        verify(userRepository).findByUsernameContainingOrEmailContaining("test", "test");
        assertEquals(2, result.size());
    }

    @Test
    void searchUsers_ShouldReturnEmptyListWhenNoMatches() {
        // Arrange
        when(userRepository.findByUsernameContainingOrEmailContaining("xyz", "xyz"))
                .thenReturn(Collections.emptyList());
        when(userConverter.convertToDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = userService.searchUsers("xyz");

        // Assert
        verify(userRepository).findByUsernameContainingOrEmailContaining("xyz", "xyz");
        assertTrue(result.isEmpty());
    }

    @Test
    void searchUsers_ShouldHandleEmptySearchTerm() {
        // Arrange
        List<User> allUsers = Arrays.asList(testUser, testUser2, testUser3);
        List<UserDTO> allUserDTOs = Arrays.asList(testUserDTO, testUserDTO2, testUserDTO3);
        when(userRepository.findByUsernameContainingOrEmailContaining("", ""))
                .thenReturn(allUsers);
        when(userConverter.convertToDTOList(allUsers)).thenReturn(allUserDTOs);

        // Act
        List<UserDTO> result = userService.searchUsers("");

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
        Optional<UserDTO> result = userService.authenticateUser("testuser", "password123");

        // Assert
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "password123");
        assertTrue(result.isPresent());
        assertEquals(testUserDTO, result.get());
    }

    @Test
    void authenticateUser_ShouldReturnEmptyWhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.authenticateUser("nonexistent", "password123");

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
        Optional<UserDTO> result = userService.authenticateUser("testuser", "wrongpassword");

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
        List<UserDTO> developerDTOs = List.of(testUserDTO);
        when(userRepository.findByRole(User.Role.DEVELOPER)).thenReturn(developers);
        when(userConverter.convertToDTOList(developers)).thenReturn(developerDTOs);

        // Act
        List<UserDTO> result = userService.getUsersByRole(User.Role.DEVELOPER);

        // Assert
        verify(userRepository).findByRole(User.Role.DEVELOPER);
        assertEquals(1, result.size());
        assertEquals(testUserDTO, result.get(0));
    }

    @Test
    void getUsersByRole_ShouldReturnEmptyListWhenNoUsersWithRole() {
        // Arrange
        when(userRepository.findByRole(User.Role.TESTER)).thenReturn(Collections.emptyList());
        when(userConverter.convertToDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = userService.getUsersByRole(User.Role.TESTER);

        // Assert
        verify(userRepository).findByRole(User.Role.TESTER);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByRoles_ShouldReturnUsersWithSpecificRoles() {
        // Arrange
        List<User> users = Arrays.asList(testUser, testUser2);
        List<UserDTO> userDTOs = Arrays.asList(testUserDTO, testUserDTO2);
        List<User.Role> roles = Arrays.asList(User.Role.DEVELOPER, User.Role.PRODUCT_OWNER);

        when(userRepository.findByRoleIn(roles)).thenReturn(users);
        when(userConverter.convertToDTOList(users)).thenReturn(userDTOs);

        // Act
        List<UserDTO> result = userService.getUsersByRoles(roles);

        // Assert
        verify(userRepository).findByRoleIn(roles);
        assertEquals(2, result.size());
    }

    @Test
    void getUsersByRoles_ShouldReturnEmptyListWhenNoMatchingRoles() {
        // Arrange
        List<User.Role> roles = Arrays.asList(User.Role.TESTER, User.Role.ADMIN);
        when(userRepository.findByRoleIn(roles)).thenReturn(Collections.emptyList());
        when(userConverter.convertToDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = userService.getUsersByRoles(roles);

        // Assert
        verify(userRepository).findByRoleIn(roles);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByRoles_ShouldHandleEmptyRolesList() {
        // Arrange
        List<User.Role> emptyRoles = Collections.emptyList();
        when(userRepository.findByRoleIn(emptyRoles)).thenReturn(Collections.emptyList());
        when(userConverter.convertToDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = userService.getUsersByRoles(emptyRoles);

        // Assert
        verify(userRepository).findByRoleIn(emptyRoles);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByProjectId_ShouldReturnUsersAssociatedWithProject() {
        // Arrange
        when(projectRepository.findById(1)).thenReturn(Optional.of(testProject));
        when(userRepository.findUsersByProjectId(1)).thenReturn(List.of(testUser));
        when(userConverter.convertToDTOList(List.of(testUser))).thenReturn(List.of(testUserDTO));

        // Act
        List<UserDTO> result = userService.getUsersByProjectId(1L);

        // Assert
        verify(projectRepository).findById(1);
        verify(userRepository).findUsersByProjectId(1);
        assertEquals(1, result.size());
        assertEquals(testUserDTO, result.get(0));
    }

    @Test
    void getUsersByProjectId_ShouldReturnEmptyListWhenProjectNotFound() {
        // Arrange
        when(projectRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        List<UserDTO> result = userService.getUsersByProjectId(99L);

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
        when(userRepository.findUsersByProjectId(3)).thenReturn(Collections.emptyList());
        when(userConverter.convertToDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = userService.getUsersByProjectId(3L);

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

    // Additional tests for methods not covered yet

    @Test
    void getUsersByUserStoryId_ShouldReturnUsersAssociatedWithUserStory() {
        // Arrange
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(testUserStory));
        when(userRepository.findUsersByUserStoryId(1L)).thenReturn(List.of(testUser));
        when(userConverter.convertToDTOList(List.of(testUser))).thenReturn(List.of(testUserDTO));

        // Act
        List<UserDTO> result = userService.getUsersByUserStoryId(1L);

        // Assert
        verify(userStoryRepository).findById(1L);
        verify(userRepository).findUsersByUserStoryId(1L);
        assertEquals(1, result.size());
        assertEquals(testUserDTO, result.get(0));
    }

    @Test
    void getUsersByUserStoryId_ShouldReturnEmptyListWhenUserStoryNotFound() {
        // Arrange
        when(userStoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        List<UserDTO> result = userService.getUsersByUserStoryId(99L);

        // Assert
        verify(userStoryRepository).findById(99L);
        verify(userRepository, never()).findUsersByUserStoryId(anyLong());
        assertTrue(result.isEmpty());
    }

    @Test
    void findUsersWithoutTasks_ShouldReturnUsersWithNoTasks() {
        // Arrange
        List<User> usersWithoutTasks = List.of(testUser3);
        List<UserDTO> userDTOsWithoutTasks = List.of(testUserDTO3);
        when(userRepository.findUsersWithoutTasks()).thenReturn(usersWithoutTasks);
        when(userConverter.convertToDTOList(usersWithoutTasks)).thenReturn(userDTOsWithoutTasks);

        // Act
        List<UserDTO> result = userService.findUsersWithoutTasks();

        // Assert
        verify(userRepository).findUsersWithoutTasks();
        assertEquals(1, result.size());
        assertEquals(testUserDTO3, result.get(0));
    }

    @Test
    void findUsersWithoutTasks_ShouldReturnEmptyListWhenAllUsersHaveTasks() {
        // Arrange
        when(userRepository.findUsersWithoutTasks()).thenReturn(Collections.emptyList());
        when(userConverter.convertToDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = userService.findUsersWithoutTasks();

        // Assert
        verify(userRepository).findUsersWithoutTasks();
        assertTrue(result.isEmpty());
    }

    @Test
    void hasAssignedTasks_ShouldReturnTrueWhenUserHasTasks() {
        // Arrange
        when(userRepository.hasAssignedTasks(1L)).thenReturn(true);

        // Act
        boolean result = userService.hasAssignedTasks(1L);

        // Assert
        verify(userRepository).hasAssignedTasks(1L);
        assertTrue(result);
    }

    @Test
    void hasAssignedTasks_ShouldReturnFalseWhenUserHasNoTasks() {
        // Arrange
        when(userRepository.hasAssignedTasks(3L)).thenReturn(false);

        // Act
        boolean result = userService.hasAssignedTasks(3L);

        // Assert
        verify(userRepository).hasAssignedTasks(3L);
        assertFalse(result);
    }
}