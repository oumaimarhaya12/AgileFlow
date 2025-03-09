package org.example.productbacklog.controller;

import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.service.ProductBacklogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productbacklogs")
public class ProductBacklogController {

    @Autowired
    private ProductBacklogService productBacklogService;

    @PostMapping
    public ResponseEntity<ProductBacklog> addProductBacklog(@RequestBody ProductBacklog productBacklog) {
        ProductBacklog savedProductBacklog = productBacklogService.addProductBacklog(productBacklog);
        return new ResponseEntity<>(savedProductBacklog, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductBacklog> deleteProductBacklog(@PathVariable Long id) {
        // Convert Long to Integer
        ProductBacklog deletedProductBacklog = productBacklogService.deleteProductBacklog(id.intValue());
        return new ResponseEntity<>(deletedProductBacklog, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductBacklog> updateProductBacklog(@PathVariable Long id, @RequestBody ProductBacklog productBacklog) {
        // Convert Long to Integer
        ProductBacklog updatedProductBacklog = productBacklogService.updateProductBacklog(id.intValue(), productBacklog);
        return new ResponseEntity<>(updatedProductBacklog, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ProductBacklog>> getAllProductBacklogs() {
        List<ProductBacklog> productBacklogs = productBacklogService.findAll();
        return new ResponseEntity<>(productBacklogs, HttpStatus.OK);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProductBacklog>> getProductBacklogsByProject(@PathVariable int projectId) {
        List<ProductBacklog> productBacklogs = productBacklogService.findByProjectProjectId(projectId);
        return new ResponseEntity<>(productBacklogs, HttpStatus.OK);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<ProductBacklog> getProductBacklogByTitle(@PathVariable String title) {
        ProductBacklog productBacklog = productBacklogService.findProductBacklogByNom(title);
        return new ResponseEntity<>(productBacklog, HttpStatus.OK);
    }
}