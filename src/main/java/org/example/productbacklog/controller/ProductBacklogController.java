package org.example.productbacklog.controller;

import org.example.productbacklog.dto.ProductBacklogDTO;
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
    public ResponseEntity<ProductBacklogDTO> addProductBacklog(@RequestBody ProductBacklogDTO productBacklogDTO) {
        ProductBacklogDTO savedProductBacklog = productBacklogService.addProductBacklog(productBacklogDTO);
        return new ResponseEntity<>(savedProductBacklog, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductBacklogDTO> deleteProductBacklog(@PathVariable Long id) {
        // Convert Long to Integer
        ProductBacklogDTO deletedProductBacklog = productBacklogService.deleteProductBacklog(id.intValue());
        return new ResponseEntity<>(deletedProductBacklog, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductBacklogDTO> updateProductBacklog(@PathVariable Long id, @RequestBody ProductBacklogDTO productBacklogDTO) {
        // Convert Long to Integer
        ProductBacklogDTO updatedProductBacklog = productBacklogService.updateProductBacklog(id.intValue(), productBacklogDTO);
        return new ResponseEntity<>(updatedProductBacklog, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ProductBacklogDTO>> getAllProductBacklogs() {
        List<ProductBacklogDTO> productBacklogs = productBacklogService.findAll();
        return new ResponseEntity<>(productBacklogs, HttpStatus.OK);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProductBacklogDTO>> getProductBacklogsByProject(@PathVariable int projectId) {
        List<ProductBacklogDTO> productBacklogs = productBacklogService.findByProjectProjectId(projectId);
        return new ResponseEntity<>(productBacklogs, HttpStatus.OK);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<ProductBacklogDTO> getProductBacklogByTitle(@PathVariable String title) {
        ProductBacklogDTO productBacklog = productBacklogService.findProductBacklogByNom(title);
        return new ResponseEntity<>(productBacklog, HttpStatus.OK);
    }
}