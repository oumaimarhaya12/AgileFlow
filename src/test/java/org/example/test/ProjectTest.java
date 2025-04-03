package org.example.test;

import org.example.productbacklog.ProductBacklogApplication;
import org.example.productbacklog.dto.ProjectDTO;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.repository.UserRepository;
import org.example.productbacklog.service.ProjectService;
import org.example.productbacklog.service.ProductBacklogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ProductBacklogApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(TestConfig.class)
public class ProjectTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProductBacklogService productBacklogService;

    @Autowired
    private UserRepository userRepository;

    private User productOwner;
    private User developer;

    @BeforeEach
    public void setup() {
        // Create a product owner user
        productOwner = new User();
        productOwner.setUsername("product_owner");
        productOwner.setEmail("po@example.com");
        productOwner.setPassword("password");
        productOwner.setRole(User.Role.PRODUCT_OWNER);
        productOwner = userRepository.save(productOwner);

        // Create a developer user
        developer = new User();
        developer.setUsername("developer");
        developer.setEmail("dev@example.com");
        developer.setPassword("password");
        developer.setRole(User.Role.DEVELOPER);
        developer = userRepository.save(developer);
    }

    // Existing tests...

    @Test
    public void testAddProjectWithOwner() {
        Project project = new Project("Project With Owner");
        Project savedProject = projectService.addProjectWithOwner(project, productOwner.getId());

        assertNotNull(savedProject);
        assertNotNull(savedProject.getUser());
        assertEquals(productOwner.getId(), savedProject.getUser().getId());
        assertEquals(User.Role.PRODUCT_OWNER, savedProject.getUser().getRole());
    }

    @Test
    public void testAddProjectWithNonProductOwnerFails() {
        Project project = new Project("Project With Developer");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.addProjectWithOwner(project, developer.getId());
        });

        assertTrue(exception.getMessage().contains("Only users with the Product Owner role"));
    }

    @Test
    public void testAssignProjectToUser() {
        Project project = new Project("Project To Assign");
        project = projectService.addProject(project);

        boolean assigned = projectService.assignProjectToUser(project.getProjectId(), productOwner.getId());

        assertTrue(assigned);
        Project updatedProject = projectService.getProject(project.getProjectId());
        assertNotNull(updatedProject.getUser());
        assertEquals(productOwner.getId(), updatedProject.getUser().getId());
    }

    @Test
    public void testAssignProjectToNonProductOwnerFails() {
        Project project = new Project("Project To Assign To Developer");
        project = projectService.addProject(project);

        Project finalProject = project;
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.assignProjectToUser(finalProject.getProjectId(), developer.getId());
        });

        assertTrue(exception.getMessage().contains("Only users with the Product Owner role"));
    }

    @Test
    public void testRemoveUserFromProject() {
        Project project = new Project("Project To Remove User");
        project = projectService.addProjectWithOwner(project, productOwner.getId());

        boolean removed = projectService.removeUserFromProject(project.getProjectId());

        assertTrue(removed);
        Project updatedProject = projectService.getProject(project.getProjectId());
        assertNull(updatedProject.getUser());
    }

    @Test
    public void testGetProjectsByUser() {
        Project project1 = new Project("User Project 1");
        Project project2 = new Project("User Project 2");

        projectService.addProjectWithOwner(project1, productOwner.getId());
        projectService.addProjectWithOwner(project2, productOwner.getId());

        List<Project> userProjects = projectService.getProjectsByUser(productOwner.getId());

        assertNotNull(userProjects);
        assertEquals(2, userProjects.size());
        assertTrue(userProjects.stream().allMatch(p -> p.getUser().getId().equals(productOwner.getId())));
    }

    @Test
    public void testProjectStatisticsIncludesOwnerInfo() {
        Project project1 = new Project("Stats Project With Owner");
        Project project2 = new Project("Stats Project Without Owner");

        projectService.addProjectWithOwner(project1, productOwner.getId());
        projectService.addProject(project2);

        Map<String, Integer> stats = projectService.getProjectStatistics();

        assertNotNull(stats);
        assertTrue(stats.containsKey("projectsWithOwner"));
        assertTrue(stats.get("projectsWithOwner") >= 1);
        assertTrue(stats.get("totalProjects") >= stats.get("projectsWithOwner"));
    }
}