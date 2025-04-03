package org.example.test;

import org.example.productbacklog.ProductBacklogApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.service.ProductBacklogService;
import org.example.productbacklog.service.SprintBacklogService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ProductBacklogApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(TestConfig.class)
public class ProductBacklogServiceTest {

    @Autowired
    private ProductBacklogService productBacklogService;

    @Autowired
    private SprintBacklogService sprintBacklogService;

    @Test
    public void testAddProductBacklog() {
        ProductBacklog productBacklog = new ProductBacklog("Test Product Backlog", new ArrayList<>(), null);
        ProductBacklog savedBacklog = productBacklogService.addProductBacklog(productBacklog);

        assertNotNull(savedBacklog);
        assertNotNull(savedBacklog.getId());
        assertEquals("Test Product Backlog", savedBacklog.getTitle());
    }

    @Test
    @Transactional
    public void testAddSprintBacklogToProductBacklog() {
        // Create a product backlog
        ProductBacklog productBacklog = new ProductBacklog("Product Backlog For Sprint", new ArrayList<>(), null);
        productBacklog = productBacklogService.addProductBacklog(productBacklog);

        // Create a sprint backlog
        SprintBacklog sprintBacklog = sprintBacklogService.createSprintBacklog("Test Sprint Backlog");

        // Add sprint backlog to product backlog
        boolean result = productBacklogService.addSprintBacklogToProductBacklog(productBacklog.getId(), sprintBacklog.getId());

        // Verify the sprint backlog was added to the product backlog
        assertTrue(result);

        // Get the product backlog and verify it contains the sprint backlog
        List<SprintBacklog> sprintBacklogs = productBacklogService.getAllSprintBacklogsByProductBacklog(productBacklog.getId());

        assertNotNull(sprintBacklogs);
        assertFalse(sprintBacklogs.isEmpty());
        assertTrue(sprintBacklogs.stream().anyMatch(sb -> sb.getId().equals(sprintBacklog.getId())));
    }

    @Test
    @Transactional
    public void testRemoveSprintBacklogFromProductBacklog() {
        // Create a product backlog
        ProductBacklog productBacklog = new ProductBacklog("Product Backlog For Sprint Removal", new ArrayList<>(), null);
        productBacklog = productBacklogService.addProductBacklog(productBacklog);

        // Create a sprint backlog
        SprintBacklog sprintBacklog = sprintBacklogService.createSprintBacklog("Sprint To Remove");

        // Add sprint backlog to product backlog
        productBacklogService.addSprintBacklogToProductBacklog(productBacklog.getId(), sprintBacklog.getId());

        // Remove sprint backlog from product backlog
        boolean result = productBacklogService.removeSprintBacklogFromProductBacklog(productBacklog.getId(), sprintBacklog.getId());

        // Verify the sprint backlog was removed
        assertTrue(result);

        // Get the product backlog's sprint backlogs and verify it no longer contains the sprint backlog
        List<SprintBacklog> sprintBacklogs = productBacklogService.getAllSprintBacklogsByProductBacklog(productBacklog.getId());
        assertTrue(sprintBacklogs.stream().noneMatch(sb -> sb.getId().equals(sprintBacklog.getId())));
    }

    @Test
    @Transactional
    public void testGetAllSprintBacklogsByProductBacklog() {
        // Create a product backlog
        ProductBacklog productBacklog = new ProductBacklog("Product Backlog For Sprint List", new ArrayList<>(), null);
        productBacklog = productBacklogService.addProductBacklog(productBacklog);

        // Create multiple sprint backlogs
        SprintBacklog sprint1 = sprintBacklogService.createSprintBacklog("Sprint 1");
        SprintBacklog sprint2 = sprintBacklogService.createSprintBacklog("Sprint 2");

        // Add sprint backlogs to product backlog
        productBacklogService.addSprintBacklogToProductBacklog(productBacklog.getId(), sprint1.getId());
        productBacklogService.addSprintBacklogToProductBacklog(productBacklog.getId(), sprint2.getId());

        // Get all sprint backlogs for the product backlog
        List<SprintBacklog> sprintBacklogs = productBacklogService.getAllSprintBacklogsByProductBacklog(productBacklog.getId());

        // Verify the list contains both sprint backlogs
        assertNotNull(sprintBacklogs);
        assertEquals(2, sprintBacklogs.size());
        assertTrue(sprintBacklogs.stream().anyMatch(sb -> sb.getId().equals(sprint1.getId())));
        assertTrue(sprintBacklogs.stream().anyMatch(sb -> sb.getId().equals(sprint2.getId())));
    }
}