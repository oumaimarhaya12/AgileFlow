package org.example.productbacklog.service;

import org.example.productbacklog.dto.ProductBacklogDTO;
import org.example.productbacklog.dto.SprintBacklogDTO;
import org.example.productbacklog.entity.SprintBacklog;
import java.util.List;
import java.util.Map;

public interface ProductBacklogService {
    ProductBacklogDTO addProductBacklog(ProductBacklogDTO productBacklogDTO);
    ProductBacklogDTO findProductBacklogByNom(String nom);
    ProductBacklogDTO deleteProductBacklog(Integer id);
    ProductBacklogDTO updateProductBacklog(Integer id, ProductBacklogDTO productBacklogDTO);
    List<ProductBacklogDTO> findByProjectProjectId(int projectId);
    List<ProductBacklogDTO> findAll();

    // Methods for SprintBacklog relationship
    boolean addSprintBacklogToProductBacklog(Integer productBacklogId, Long sprintBacklogId);
    boolean removeSprintBacklogFromProductBacklog(Integer productBacklogId, Long sprintBacklogId);

    // Change this method to return DTOs instead of entities
    List<SprintBacklogDTO> getAllSprintBacklogsByProductBacklog(Integer productBacklogId);

    Map<String, Integer> getProductBacklogSprintStatistics(Integer productBacklogId);

    // Add these methods for the test cases
    List<SprintBacklogDTO> getSprintBacklogsByProductBacklogId(Integer productBacklogId);
    List<SprintBacklogDTO> getActiveSprintBacklogsByProductBacklogId(Integer productBacklogId);
    List<SprintBacklogDTO> getCompletedSprintBacklogsByProductBacklogId(Integer productBacklogId);
}