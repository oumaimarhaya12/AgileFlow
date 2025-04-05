package org.example.test;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.example.productbacklog.converter.ProjectConverter;
import org.example.productbacklog.dto.ProjectDTO;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.repository.ProjectRepository;
import org.example.productbacklog.repository.UserRepository;
import org.example.productbacklog.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProductBacklogRepository productBacklogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectConverter projectConverter;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;
    private ProjectDTO projectDTO;
    private User productOwner;
    private User developer;
    private ProductBacklog productBacklog;

    @BeforeEach
    void setUp() {
        // Set up Project
        project = new Project();
        project.setProjectId(1);
        project.setProjectName("Test Project");

        // Set up ProjectDTO
        projectDTO = ProjectDTO.builder()
                .projectId(1)
                .projectName("Test Project")
                .build();

        // Set up Product Owner
        productOwner = new User();
        productOwner.setId(1L);
        productOwner.setUsername("product_owner");
        productOwner.setEmail("po@example.com");
        productOwner.setRole(User.Role.PRODUCT_OWNER);

        // Set up Developer
        developer = new User();
        developer.setId(2L);
        developer.setUsername("developer");
        developer.setEmail("dev@example.com");
        developer.setRole(User.Role.DEVELOPER);

        // Set up ProductBacklog
        productBacklog = new ProductBacklog();
        productBacklog.setId(1);
        productBacklog.setTitle("Test Product Backlog");

        // Set up converter mocks
        when(projectConverter.convertToDTO(project)).thenReturn(projectDTO);
        when(projectConverter.convertToEntity(projectDTO)).thenReturn(project);
    }

    @Test
    void testAddProject() {
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectDTO result = projectService.addProject(projectDTO);

        assertNotNull(result);
        assertEquals(projectDTO.getProjectName(), result.getProjectName());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void testAddProjectWithOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(productOwner));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectDTO result = projectService.addProjectWithOwner(projectDTO, 1L);

        assertNotNull(result);
        assertEquals(projectDTO.getProjectName(), result.getProjectName());
        verify(userRepository).findById(1L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void testAddProjectWithOwner_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.addProjectWithOwner(projectDTO, 99L);
        });

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(99L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void testAddProjectWithOwner_NotProductOwner() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.addProjectWithOwner(projectDTO, 2L);
        });

        assertTrue(exception.getMessage().contains("Only users with the Product Owner role"));
        verify(userRepository).findById(2L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void testUpdateProject() {
        Project existingProject = new Project();
        existingProject.setProjectId(1);
        existingProject.setProjectName("Original Name");

        ProjectDTO updatedDTO = ProjectDTO.builder()
                .projectId(1)
                .projectName("Updated Name")
                .build();

        Project updatedProject = new Project();
        updatedProject.setProjectId(1);
        updatedProject.setProjectName("Updated Name");

        when(projectRepository.findById(1)).thenReturn(Optional.of(existingProject));
        when(projectConverter.updateEntityFromDTO(existingProject, updatedDTO)).thenReturn(updatedProject);
        when(projectRepository.save(updatedProject)).thenReturn(updatedProject);
        when(projectConverter.convertToDTO(updatedProject)).thenReturn(updatedDTO);

        ProjectDTO result = projectService.updateProject(updatedDTO, 1);

        assertNotNull(result);
        assertEquals("Updated Name", result.getProjectName());
        verify(projectRepository).findById(1);
        verify(projectConverter).updateEntityFromDTO(existingProject, updatedDTO);
        verify(projectRepository).save(updatedProject);
    }

    @Test
    void testUpdateProject_NotFound() {
        when(projectRepository.findById(99)).thenReturn(Optional.empty());

        ProjectDTO result = projectService.updateProject(projectDTO, 99);

        assertNull(result);
        verify(projectRepository).findById(99);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void testGetProject() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));

        ProjectDTO result = projectService.getProject(1);

        assertNotNull(result);
        assertEquals(projectDTO.getProjectName(), result.getProjectName());
        verify(projectRepository).findById(1);
    }

    @Test
    void testGetProject_NotFound() {
        when(projectRepository.findById(99)).thenReturn(Optional.empty());

        ProjectDTO result = projectService.getProject(99);

        assertNull(result);
        verify(projectRepository).findById(99);
    }

    @Test
    void testGetProjects() {
        List<Project> projects = Arrays.asList(project);
        List<ProjectDTO> projectDTOs = Arrays.asList(projectDTO);

        when(projectRepository.findAll()).thenReturn(projects);
        when(projectConverter.convertToDTOList(projects)).thenReturn(projectDTOs);

        List<ProjectDTO> result = projectService.getProjects();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(projectDTO.getProjectName(), result.get(0).getProjectName());
        verify(projectRepository).findAll();
    }

    @Test
    void testGetProjectsByUser() {
        List<Project> projects = Arrays.asList(project);
        List<ProjectDTO> projectDTOs = Arrays.asList(projectDTO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(productOwner));
        when(projectRepository.findByUser(productOwner)).thenReturn(projects);
        when(projectConverter.convertToDTOList(projects)).thenReturn(projectDTOs);

        List<ProjectDTO> result = projectService.getProjectsByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(projectDTO.getProjectName(), result.get(0).getProjectName());
        verify(userRepository).findById(1L);
        verify(projectRepository).findByUser(productOwner);
    }

    @Test
    void testGetProjectsByUser_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        List<ProjectDTO> result = projectService.getProjectsByUser(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findById(99L);
        verify(projectRepository, never()).findByUser(any(User.class));
    }

    @Test
    void testDeleteProject() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        doNothing().when(projectRepository).delete(project);

        boolean result = projectService.deleteProject(1);

        assertTrue(result);
        verify(projectRepository).findById(1);
        verify(projectRepository).delete(project);
    }

    @Test
    void testDeleteProject_NotFound() {
        when(projectRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = projectService.deleteProject(99);

        assertFalse(result);
        verify(projectRepository).findById(99);
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void testGetProjectByName() {
        when(projectRepository.findByProjectName("Test Project")).thenReturn(project);

        ProjectDTO result = projectService.getProjectByName("Test Project");

        assertNotNull(result);
        assertEquals(projectDTO.getProjectName(), result.getProjectName());
        verify(projectRepository).findByProjectName("Test Project");
    }

    @Test
    void testAssignProjectToUser() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(productOwner));
        when(projectRepository.save(project)).thenReturn(project);

        boolean result = projectService.assignProjectToUser(1, 1L);

        assertTrue(result);
        assertEquals(productOwner, project.getUser());
        verify(projectRepository).findById(1);
        verify(userRepository).findById(1L);
        verify(projectRepository).save(project);
    }

    @Test
    void testAssignProjectToUser_ProjectNotFound() {
        when(projectRepository.findById(99)).thenReturn(Optional.empty());
        // Since the implementation still calls findById on userRepository, we need to mock it
        when(userRepository.findById(1L)).thenReturn(Optional.of(productOwner));

        boolean result = projectService.assignProjectToUser(99, 1L);

        assertFalse(result);
        verify(projectRepository).findById(99);
        // We now expect this to be called, based on the implementation
        verify(userRepository).findById(1L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void testAssignProjectToUser_UserNotFound() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = projectService.assignProjectToUser(1, 99L);

        assertFalse(result);
        verify(projectRepository).findById(1);
        verify(userRepository).findById(99L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void testAssignProjectToUser_NotProductOwner() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.assignProjectToUser(1, 2L);
        });

        assertTrue(exception.getMessage().contains("Only users with the Product Owner role"));
        verify(projectRepository).findById(1);
        verify(userRepository).findById(2L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void testRemoveUserFromProject() {
        project.setUser(productOwner);
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);

        boolean result = projectService.removeUserFromProject(1);

        assertTrue(result);
        assertNull(project.getUser());
        verify(projectRepository).findById(1);
        verify(projectRepository).save(project);
    }

    @Test
    void testRemoveUserFromProject_ProjectNotFound() {
        when(projectRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = projectService.removeUserFromProject(99);

        assertFalse(result);
        verify(projectRepository).findById(99);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void testLinkProjectToBacklog() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(productBacklogRepository.findById(1)).thenReturn(Optional.of(productBacklog));
        when(projectRepository.save(project)).thenReturn(project);
        when(productBacklogRepository.save(productBacklog)).thenReturn(productBacklog);

        boolean result = projectService.linkProjectToBacklog(1, 1);

        assertTrue(result);
        assertEquals(productBacklog, project.getProductBacklog());
        assertEquals(project, productBacklog.getProject());
        verify(projectRepository).findById(1);
        verify(productBacklogRepository).findById(1);
        verify(projectRepository).save(project);
        verify(productBacklogRepository).save(productBacklog);
    }

    @Test
    void testLinkProjectToBacklog_ProjectNotFound() {
        when(projectRepository.findById(99)).thenReturn(Optional.empty());
        // Since the implementation still calls findById on productBacklogRepository, we need to mock it
        when(productBacklogRepository.findById(1)).thenReturn(Optional.of(productBacklog));

        boolean result = projectService.linkProjectToBacklog(99, 1);

        assertFalse(result);
        verify(projectRepository).findById(99);
        // We now expect this to be called, based on the implementation
        verify(productBacklogRepository).findById(1);
        verify(projectRepository, never()).save(any(Project.class));
        verify(productBacklogRepository, never()).save(any(ProductBacklog.class));
    }

    @Test
    void testLinkProjectToBacklog_BacklogNotFound() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(productBacklogRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = projectService.linkProjectToBacklog(1, 99);

        assertFalse(result);
        verify(projectRepository).findById(1);
        verify(productBacklogRepository).findById(99);
        verify(projectRepository, never()).save(any(Project.class));
        verify(productBacklogRepository, never()).save(any(ProductBacklog.class));
    }

    @Test
    void testUnlinkProjectFromBacklog() {
        project.setProductBacklog(productBacklog);
        productBacklog.setProject(project);

        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        when(productBacklogRepository.save(productBacklog)).thenReturn(productBacklog);

        boolean result = projectService.unlinkProjectFromBacklog(1);

        assertTrue(result);
        assertNull(project.getProductBacklog());
        assertNull(productBacklog.getProject());
        verify(projectRepository).findById(1);
        verify(projectRepository).save(project);
        verify(productBacklogRepository).save(productBacklog);
    }

    @Test
    void testUnlinkProjectFromBacklog_ProjectNotFound() {
        when(projectRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = projectService.unlinkProjectFromBacklog(99);

        assertFalse(result);
        verify(projectRepository).findById(99);
        verify(projectRepository, never()).save(any(Project.class));
        verify(productBacklogRepository, never()).save(any(ProductBacklog.class));
    }

    @Test
    void testUnlinkProjectFromBacklog_NoBacklogLinked() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));

        boolean result = projectService.unlinkProjectFromBacklog(1);

        assertFalse(result);
        verify(projectRepository).findById(1);
        verify(projectRepository, never()).save(any(Project.class));
        verify(productBacklogRepository, never()).save(any(ProductBacklog.class));
    }

    @Test
    void testGetProjectStatistics() {
        Project project1 = new Project();
        project1.setProjectId(1);
        project1.setProjectName("Project 1");
        project1.setUser(productOwner);
        project1.setProductBacklog(productBacklog);

        Project project2 = new Project();
        project2.setProjectId(2);
        project2.setProjectName("Project 2");
        project2.setUser(productOwner);

        Project project3 = new Project();
        project3.setProjectId(3);
        project3.setProjectName("Project 3");

        List<Project> projects = Arrays.asList(project1, project2, project3);
        when(projectRepository.findAll()).thenReturn(projects);

        Map<String, Integer> result = projectService.getProjectStatistics();

        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(3, result.get("totalProjects"));
        assertEquals(1, result.get("projectsWithBacklog"));
        assertEquals(2, result.get("projectsWithoutBacklog"));
        assertEquals(2, result.get("projectsWithOwner"));
        verify(projectRepository).findAll();
    }
}