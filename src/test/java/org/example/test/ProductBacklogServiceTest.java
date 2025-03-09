package org.example.test;

import org.example.productbacklog.ProductBacklogApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.service.impl.ProductBacklogServiceImpl;
import org.example.productbacklog.repository.ProjectRepository;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ProductBacklogApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(TestConfig.class)
class ProductBacklogServiceImplTest {

    @Autowired
    private ProductBacklogServiceImpl productBacklogService;

    @Autowired
    private ProjectRepository projectRepository;

    private Project project;

    @BeforeEach
    void init() {
        // Create and save a project first
        project = new Project("Test Project");
        project = projectRepository.save(project); // Save the project
    }

    @Test
    void ajouter() {
        // Create a product backlog with empty epics list and the saved project
        ProductBacklog productBacklog = new ProductBacklog("Add Test Backlog", new ArrayList<>(), project);
        ProductBacklog result = productBacklogService.addProductBacklog(productBacklog);
        assertNotNull(result);
        assertEquals(productBacklog.getTitle(), result.getTitle());
        assertNotNull(result.getProject());
    }

    @Test
    void modifier() {
        // Create a product backlog with empty epics list and the saved project
        ProductBacklog productBacklog = new ProductBacklog("Update Test Backlog", new ArrayList<>(), project);

        // First add the product backlog to get an ID
        ProductBacklog saved = productBacklogService.addProductBacklog(productBacklog);

        // Now update it
        saved.setTitle("Updated Product Backlog");
        ProductBacklog result = productBacklogService.updateProductBacklog(saved.getId(), saved);

        assertNotNull(result);
        assertEquals("Updated Product Backlog", result.getTitle());
    }

    @Test
    void find() {
        // Create a product backlog with a unique title and the saved project
        String uniqueTitle = "Find Test Backlog";
        ProductBacklog productBacklog = new ProductBacklog(uniqueTitle, new ArrayList<>(), project);

        // First add the product backlog
        productBacklogService.addProductBacklog(productBacklog);

        // Now find it
        ProductBacklog result = productBacklogService.findProductBacklogByNom(uniqueTitle);
        assertNotNull(result);
        assertEquals(uniqueTitle, result.getTitle());
    }

    @Test
    void remover() {
        // Create a product backlog with a unique title and the saved project
        ProductBacklog productBacklog = new ProductBacklog("Remove Test Backlog", new ArrayList<>(), project);

        // First add the product backlog to get an ID
        ProductBacklog saved = productBacklogService.addProductBacklog(productBacklog);

        // Now delete it
        ProductBacklog result = productBacklogService.deleteProductBacklog(saved.getId());

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
    }
}