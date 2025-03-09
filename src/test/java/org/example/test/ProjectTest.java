package org.example.test;

import org.example.productbacklog.ProductBacklogApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.service.ProjectService;
import org.example.productbacklog.service.ProductBacklogService;
import org.example.productbacklog.dto.ProjectDTO;
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

    @Test
    public void ajouter() {
        Project project = new Project("Add Test Project");
        Project projectTest = projectService.addProject(project);
        assertNotNull(projectTest);
        assertNotNull(projectTest.getProjectId());
        assertEquals("Add Test Project", projectTest.getProjectName());
    }

    @Test
    public void modifier() {
        Project project = new Project("Update Test Project");
        Project savedProject = projectService.addProject(project);
        ProjectDTO projectDTO = new ProjectDTO(savedProject.getProjectId(), "Updated Project Name");
        Project updatedProject = projectService.updateProject(projectDTO, projectDTO.getIdProjet());
        assertNotNull(updatedProject);
        assertEquals("Updated Project Name", updatedProject.getProjectName());
        assertEquals(savedProject.getProjectId(), updatedProject.getProjectId());
    }

    @Test
    public void getProject() {
        Project project = new Project("Get Test Project");
        Project savedProject = projectService.addProject(project);
        Project projectTest = projectService.getProject(savedProject.getProjectId());
        assertNotNull(projectTest);
        assertEquals(savedProject.getProjectId(), projectTest.getProjectId());
        assertEquals("Get Test Project", projectTest.getProjectName());
    }

    @Test
    public void getProjects() {
        projectService.addProject(new Project("List Test Project 1"));
        projectService.addProject(new Project("List Test Project 2"));
        List<Project> projectsTest = projectService.getProjects();
        assertNotNull(projectsTest);
        assertFalse(projectsTest.isEmpty());
        assertTrue(projectsTest.size() >= 2);
    }

    @Test
    public void getProjectByName() {
        String uniqueName = "Name Test Project " + System.currentTimeMillis();
        Project project = new Project(uniqueName);
        projectService.addProject(project);
        Project projectTest = projectService.getProjectByName(uniqueName);
        assertNotNull(projectTest);
        assertEquals(uniqueName, projectTest.getProjectName());
    }

    @Test
    public void deleteProject() {
        Project project = new Project("Delete Test Project");
        Project savedProject = projectService.addProject(project);
        Project deletedProject = projectService.deleteProject(savedProject.getProjectId());
        assertNotNull(deletedProject);
        assertEquals(savedProject.getProjectId(), deletedProject.getProjectId());
        assertNull(projectService.getProject(savedProject.getProjectId()));
    }

    @Test
    public void testProjectWithBacklog() {
        Project project = new Project("Project With Backlog");
        project = projectService.addProject(project);
        ProductBacklog backlog = new ProductBacklog("Test Backlog", new ArrayList<>(), null);
        backlog = productBacklogService.addProductBacklog(backlog);
        boolean linked = projectService.linkProjectToBacklog(project.getProjectId(), backlog.getId());
        assertTrue(linked);
        Project updatedProject = projectService.getProject(project.getProjectId());
        assertNotNull(updatedProject.getProductBacklog());
        assertEquals(backlog.getId(), updatedProject.getProductBacklog().getId());
        boolean unlinked = projectService.unlinkProjectFromBacklog(project.getProjectId());
        assertTrue(unlinked);
        updatedProject = projectService.getProject(project.getProjectId());
        assertNull(updatedProject.getProductBacklog());
    }

    @Test
    public void testProjectStatistics() {
        projectService.addProject(new Project("Stats Project 1"));
        projectService.addProject(new Project("Stats Project 2"));
        Map<String, Integer> stats = projectService.getProjectStatistics();
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalProjects"));
        assertTrue(stats.containsKey("projectsWithBacklog"));
        assertTrue(stats.containsKey("projectsWithoutBacklog"));
        assertTrue(stats.get("totalProjects") >= 2);
        assertEquals(stats.get("totalProjects"),
                stats.get("projectsWithBacklog") + stats.get("projectsWithoutBacklog"));
    }
}