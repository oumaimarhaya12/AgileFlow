package org.example.productbacklog.service;

import org.example.productbacklog.entity.ProductBacklog;

public interface ProductBacklogService {
    ProductBacklog addProductBacklog(ProductBacklog productBacklog);
    ProductBacklog findProductBacklogByNom(String nom);
    ProductBacklog deleteProductBacklog(Long id);  // Keeping Long as per requirement
    ProductBacklog updateProductBacklog(Long id, ProductBacklog productBacklog);  // Keeping Long as per requirement
}