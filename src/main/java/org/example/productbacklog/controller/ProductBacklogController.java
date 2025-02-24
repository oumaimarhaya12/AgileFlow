package org.example.productbacklog.controller;

import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.service.ProductBacklogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-backlogs")
public class ProductBacklogController {

    private final ProductBacklogService productBacklogService;

    @Autowired
    public ProductBacklogController(ProductBacklogService productBacklogService) {
        this.productBacklogService = productBacklogService;
    }

    @PostMapping
    public ResponseEntity<ProductBacklog> createProductBacklog(@RequestBody ProductBacklog productBacklog) {
        return ResponseEntity.ok(productBacklogService.createProductBacklog(productBacklog));
    }

    @GetMapping
    public ResponseEntity<List<ProductBacklog>> getAllProductBacklogs() {
        return ResponseEntity.ok(productBacklogService.getAllProductBacklogs());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductBacklog> updateProductBacklog(@PathVariable Long id, @RequestBody ProductBacklog productBacklog) {
        return ResponseEntity.ok(productBacklogService.updateProductBacklog(id, productBacklog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductBacklog(@PathVariable Long id) {
        productBacklogService.deleteProductBacklog(id);
        return ResponseEntity.noContent().build();
    }
}
