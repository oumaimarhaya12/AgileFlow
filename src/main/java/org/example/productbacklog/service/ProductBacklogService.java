package org.example.productbacklog.service;

import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.SprintBacklog;
import java.util.List;
import java.util.Map;

public interface ProductBacklogService {
    ProductBacklog addProductBacklog(ProductBacklog productBacklog);
    ProductBacklog findProductBacklogByNom(String nom);
    ProductBacklog deleteProductBacklog(Integer id);
    ProductBacklog updateProductBacklog(Integer id, ProductBacklog productBacklog);
    List<ProductBacklog> findByProjectProjectId(int projectId);
    List<ProductBacklog> findAll();

    // Methods for SprintBacklog relationship
    boolean addSprintBacklogToProductBacklog(Integer productBacklogId, Long sprintBacklogId);
    boolean removeSprintBacklogFromProductBacklog(Integer productBacklogId, Long sprintBacklogId);
    List<SprintBacklog> getAllSprintBacklogsByProductBacklog(Integer productBacklogId);
    Map<String, Integer> getProductBacklogSprintStatistics(Integer productBacklogId);
}