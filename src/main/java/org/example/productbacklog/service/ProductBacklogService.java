package org.example.productbacklog.service;

import org.example.productbacklog.entity.ProductBacklog;
import java.util.List;

public interface ProductBacklogService {
    ProductBacklog createProductBacklog(ProductBacklog productBacklog);
    List<ProductBacklog> getAllProductBacklogs();
    ProductBacklog updateProductBacklog(Long id, ProductBacklog productBacklog);
    void deleteProductBacklog(Long id);
}