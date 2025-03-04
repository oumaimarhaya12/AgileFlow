package org.example.productbacklog.service.impl;

import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.service.ProductBacklogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ProductBacklogServiceImpl implements ProductBacklogService {

    @Autowired
    private ProductBacklogRepository productBacklogRepo;

    @Override
    public ProductBacklog addProductBacklog(ProductBacklog productBacklog) {
        return productBacklogRepo.save(productBacklog);
    }

    @Override
    public ProductBacklog findProductBacklogByNom(String nom) {
        Optional<ProductBacklog> backlogOptional = productBacklogRepo.findFirstByTitle(nom);
        if (backlogOptional.isEmpty()) {
            throw new IllegalStateException("ProductBacklog not found");
        }
        return backlogOptional.get();
    }

    @Override
    public ProductBacklog deleteProductBacklog(Long id) {
        Optional<ProductBacklog> existingBacklogOptional = productBacklogRepo.findById(Math.toIntExact(id));
        if (existingBacklogOptional.isPresent()) {
            ProductBacklog backlogToDelete = existingBacklogOptional.get();
            productBacklogRepo.delete(backlogToDelete);
            return backlogToDelete;
        }
        throw new IllegalStateException("ProductBacklog not found");
    }

    @Override
    public ProductBacklog updateProductBacklog(Long id, ProductBacklog productBacklog) {
        Optional<ProductBacklog> existingBacklogOptional = productBacklogRepo.findById(Math.toIntExact(id));
        if (existingBacklogOptional.isPresent()) {
            ProductBacklog existingBacklog = existingBacklogOptional.get();

            if (productBacklog.getTitle() != null) {
                existingBacklog.setTitle(productBacklog.getTitle());
            }

            return productBacklogRepo.save(existingBacklog);
        }
        throw new IllegalStateException("ProductBacklog not found");
    }
}