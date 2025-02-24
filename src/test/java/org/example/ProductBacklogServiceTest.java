package org.example;

import org.example.productbacklog.ProductBacklogApplication;
import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.service.ProductBacklogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ProductBacklogApplication.class)
@TestPropertySource(properties = {"spring.security.enabled=false"})
public class ProductBacklogServiceTest {

    @Autowired
    private ProductBacklogService productBacklogService;

    @Autowired
    private ProductBacklogRepository productBacklogRepository;

    private ProductBacklog testProductBacklog;

    @BeforeEach
    void setUp() {
        // Clear all product backlogs before each test
        productBacklogRepository.deleteAll(); // Ensure the database is clean before each test

        testProductBacklog = new ProductBacklog();
        testProductBacklog.setTitreProductBL("Test Product Backlog");
    }

    @Test
    void testCreateProductBacklog() {
        // Create and save a product backlog
        ProductBacklog savedBacklog = productBacklogService.createProductBacklog(testProductBacklog);

        // Check if the ID is not null and the title matches
        assertNotNull(savedBacklog.getId(), "ID should not be null after saving");
        assertEquals("Test Product Backlog", savedBacklog.getTitreProductBL(), "Product backlog title should match");
    }

    @Test
    void testGetAllProductBacklogs() {
        // Create and save the test product backlog
        productBacklogService.createProductBacklog(testProductBacklog);

        // Ensure only one product backlog exists
        assertEquals(1, productBacklogService.getAllProductBacklogs().size(), "There should be 1 product backlog");

        // Optional: You can print or log the size if needed to verify
        System.out.println("Product backlog count: " + productBacklogService.getAllProductBacklogs().size());
    }
}