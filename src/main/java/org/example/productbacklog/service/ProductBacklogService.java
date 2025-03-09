package org.example.productbacklog.service;

import org.example.productbacklog.entity.ProductBacklog;
import java.util.List;

public interface ProductBacklogService {
    ProductBacklog addProductBacklog(ProductBacklog productBacklog);
    ProductBacklog findProductBacklogByNom(String nom);
    ProductBacklog deleteProductBacklog(Integer id);
    ProductBacklog updateProductBacklog(Integer id, ProductBacklog productBacklog);
    List<ProductBacklog> findByProjectProjectId(int projectId);
    List<ProductBacklog> findAll();
}