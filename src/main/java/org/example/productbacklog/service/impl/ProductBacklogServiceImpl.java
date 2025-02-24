package org.example.productbacklog.service.impl;

import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.service.ProductBacklogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductBacklogServiceImpl implements ProductBacklogService {

    @Autowired
    private ProductBacklogRepository productBacklogRepository;

    @Override
    public ProductBacklog createProductBacklog(ProductBacklog productBacklog) {
        return productBacklogRepository.save(productBacklog);
    }

    @Override
    public List<ProductBacklog> getAllProductBacklogs() {
        return productBacklogRepository.findAll();
    }

    @Override
    public ProductBacklog updateProductBacklog(Long id, ProductBacklog productBacklog) {
        Optional<ProductBacklog> existingBacklog = productBacklogRepository.findById(id);
        if (existingBacklog.isPresent()) {
            ProductBacklog backlogToUpdate = existingBacklog.get();
            backlogToUpdate.setTitreProductBL(productBacklog.getTitreProductBL());
            // update other fields as necessary
            return productBacklogRepository.save(backlogToUpdate);
        }
        return null;  // or throw an exception
    }

    @Override
    public void deleteProductBacklog(Long id) {
        productBacklogRepository.deleteById(id);
    }
}
