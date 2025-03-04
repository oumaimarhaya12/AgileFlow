package org.example.test;

import org.example.productbacklog.ProductBacklogApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.service.ProjectService;
import org.example.productbacklog.dto.ProjectDTO;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ProductBacklogApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(TestConfig.class)
public class ProjectTest {

    @Autowired
    private ProjectService projectService;

    @Test
    public void ajouter() {
        // Create a project with a unique name
        Project project = new Project("Add Test Project");

        // Test adding a project
        Project projectTest = projectService.addProject(project);

        // Assertions
        assertNotNull(projectTest, "Project should be added successfully");
        assertNotNull(projectTest.getProjectId(), "Project should have an ID after saving");
        assertEquals("Add Test Project", projectTest.getProjectName(), "Project name should match");
    }

    @Test
    public void modifier() {
        // Create a project with a unique name
        Project project = new Project("Update Test Project");

        // First add the project to the database to generate an id
        Project savedProject = projectService.addProject(project);

        // Create a ProjectDTO with the updated name and the generated id
        ProjectDTO projectDTO = new ProjectDTO(savedProject.getProjectId(), "Updated Project Name");

        // Now update the project
        Project updatedProject = projectService.updateProject(projectDTO, projectDTO.getIdProjet());

        // Assert the project is updated
        assertNotNull(updatedProject, "Project should be updated successfully");
        assertEquals("Updated Project Name", updatedProject.getProjectName(), "Project name should be updated");
        assertEquals(savedProject.getProjectId(), updatedProject.getProjectId(), "Project ID should remain the same");
    }

    @Test
    public void getProject() {
        // Create a project with a unique name
        Project project = new Project("Get Test Project");

        // First add the project to get an ID
        Project savedProject = projectService.addProject(project);

        // Test getting a project by its ID
        Project projectTest = projectService.getProject(savedProject.getProjectId());

        // Assertions
        assertNotNull(projectTest, "Project should exist");
        assertEquals(savedProject.getProjectId(), projectTest.getProjectId(), "Project IDs should match");
        assertEquals("Get Test Project", projectTest.getProjectName(), "Project name should match");
    }

    @Test
    public void getProjects() {
        // Create and add multiple projects with unique names
        projectService.addProject(new Project("List Test Project 1"));
        projectService.addProject(new Project("List Test Project 2"));

        // Test getting a list of all projects
        List<Project> projectsTest = projectService.getProjects();

        // Assertions
        assertNotNull(projectsTest, "Project list should not be null");
        assertFalse(projectsTest.isEmpty(), "Project list should not be empty");
        assertTrue(projectsTest.size() >= 2, "Project list should contain at least the 2 projects we added");
    }

    @Test
    public void getProjectByName() {
        // Create a project with a unique name
        String uniqueName = "Name Test Project " + System.currentTimeMillis(); // Add timestamp for uniqueness
        Project project = new Project(uniqueName);

        // First add the project
        projectService.addProject(project);

        // Test getting a project by its name
        Project projectTest = projectService.getProjectByName(uniqueName);

        // Assertions
        assertNotNull(projectTest, "Project should be found by name");
        assertEquals(uniqueName, projectTest.getProjectName(), "Project names should match");
    }

    @Test
    public void deleteProject() {
        // Create a project with a unique name
        Project project = new Project("Delete Test Project");

        // First add the project to get an ID
        Project savedProject = projectService.addProject(project);

        // Now delete it
        Project deletedProject = projectService.deleteProject(savedProject.getProjectId());

        // Assertions
        assertNotNull(deletedProject, "Deleted project should be returned");
        assertEquals(savedProject.getProjectId(), deletedProject.getProjectId(), "Project IDs should match");

        // Verify the project is actually deleted by trying to get it (should throw exception)
        try {
            projectService.getProject(savedProject.getProjectId());
            fail("Should have thrown an exception because the project was deleted");
        } catch (IllegalStateException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("n'existe pas"), "Exception message should indicate project doesn't exist");
        }
    }
}