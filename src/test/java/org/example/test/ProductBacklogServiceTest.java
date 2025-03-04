package org.example.test;

import org.example.productbacklog.ProductBacklogApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.service.impl.ProductBacklogServiceImpl;
import org.example.productbacklog.repository.ProjectRepository;
import org.springframework.test.annotation.DirtiesContext;

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
        project = projectRepository.save(project);
    }

    @Test
    void ajouter() {
        ProductBacklog productBacklog = new ProductBacklog("Add Test Backlog", null, project);
        ProductBacklog result = productBacklogService.addProductBacklog(productBacklog);
        assertNotNull(result);
        assertEquals(productBacklog.getTitle(), result.getTitle());
        assertNotNull(result.getProject());
    }

    @Test
    void modifier() {
        // Create a product backlog with a unique title
        ProductBacklog productBacklog = new ProductBacklog("Update Test Backlog", null, project);

        // First add the product backlog to get an ID
        ProductBacklog saved = productBacklogService.addProductBacklog(productBacklog);

        // Now update it
        saved.setTitle("Updated Product Backlog");
        ProductBacklog result = productBacklogService.updateProductBacklog(saved.getId().longValue(), saved);

        assertNotNull(result);
        assertEquals("Updated Product Backlog", result.getTitle());
    }

    @Test
    void find() {
        // Create a product backlog with a unique title
        String uniqueTitle = "Find Test Backlog";
        ProductBacklog productBacklog = new ProductBacklog(uniqueTitle, null, project);

        // First add the product backlog
        productBacklogService.addProductBacklog(productBacklog);

        // Now find it
        ProductBacklog result = productBacklogService.findProductBacklogByNom(uniqueTitle);
        assertNotNull(result);
        assertEquals(uniqueTitle, result.getTitle());
    }

    @Test
    void remover() {
        // Create a product backlog with a unique title
        ProductBacklog productBacklog = new ProductBacklog("Remove Test Backlog", null, project);

        // First add the product backlog to get an ID
        ProductBacklog saved = productBacklogService.addProductBacklog(productBacklog);

        // Now delete it
        ProductBacklog result = productBacklogService.deleteProductBacklog(saved.getId().longValue());

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
    }
}