package org.example.productbacklog.service.impl;

import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.repository.ProjectRepository;
import org.example.productbacklog.service.ProductBacklogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductBacklogServiceImpl implements ProductBacklogService {

    @Autowired
    private ProductBacklogRepository productBacklogRepo;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    @Transactional
    public ProductBacklog addProductBacklog(ProductBacklog productBacklog) {
        // First save the product backlog without the project reference
        if (productBacklog.getProject() != null) {
            Project project = productBacklog.getProject();

            // Temporarily set project to null to save the backlog first
            productBacklog.setProject(null);
            productBacklog = productBacklogRepo.save(productBacklog);

            // Now handle the project relationship
            if (project.getProductBacklog() != null && !project.getProductBacklog().equals(productBacklog)) {
                ProductBacklog oldBacklog = project.getProductBacklog();
                oldBacklog.setProject(null);
                productBacklogRepo.save(oldBacklog);
            }

            // Set the bidirectional relationship
            productBacklog.setProject(project);
            project.setProductBacklog(productBacklog);
            projectRepository.save(project);

            // Save the backlog again with the project reference
            return productBacklogRepo.save(productBacklog);
        }

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
    @Transactional
    public ProductBacklog deleteProductBacklog(Integer id) {
        Optional<ProductBacklog> existingBacklogOptional = productBacklogRepo.findById(id);
        if (existingBacklogOptional.isPresent()) {
            ProductBacklog backlogToDelete = existingBacklogOptional.get();
            if (backlogToDelete.getProject() != null) {
                Project project = backlogToDelete.getProject();
                project.setProductBacklog(null);
                projectRepository.save(project);
                backlogToDelete.setProject(null);
                productBacklogRepo.save(backlogToDelete);
            }
            productBacklogRepo.delete(backlogToDelete);
            return backlogToDelete;
        }
        throw new IllegalStateException("ProductBacklog not found");
    }

    @Override
    @Transactional
    public ProductBacklog updateProductBacklog(Integer id, ProductBacklog productBacklog) {
        Optional<ProductBacklog> existingBacklogOptional = productBacklogRepo.findById(id);
        if (existingBacklogOptional.isPresent()) {
            ProductBacklog existingBacklog = existingBacklogOptional.get();
            if (productBacklog.getTitle() != null) {
                existingBacklog.setTitle(productBacklog.getTitle());
            }

            // Handle project relationship changes
            if (productBacklog.getProject() != existingBacklog.getProject()) {
                // First remove the old relationship if it exists
                if (existingBacklog.getProject() != null) {
                    Project oldProject = existingBacklog.getProject();
                    oldProject.setProductBacklog(null);
                    projectRepository.save(oldProject);
                    existingBacklog.setProject(null);
                    existingBacklog = productBacklogRepo.save(existingBacklog);
                }

                // Then establish the new relationship if needed
                if (productBacklog.getProject() != null) {
                    Project newProject = productBacklog.getProject();
                    if (newProject.getProductBacklog() != null && !newProject.getProductBacklog().equals(existingBacklog)) {
                        ProductBacklog oldBacklog = newProject.getProductBacklog();
                        oldBacklog.setProject(null);
                        productBacklogRepo.save(oldBacklog);
                    }
                    existingBacklog.setProject(newProject);
                    newProject.setProductBacklog(existingBacklog);
                    projectRepository.save(newProject);
                }
            }

            if (productBacklog.getEpics() != null && !productBacklog.getEpics().isEmpty()) {
                existingBacklog.setEpics(productBacklog.getEpics());
            }

            return productBacklogRepo.save(existingBacklog);
        }
        throw new IllegalStateException("ProductBacklog not found");
    }

    @Override
    public List<ProductBacklog> findByProjectProjectId(int projectId) {
        return productBacklogRepo.findByProjectProjectId(projectId);
    }

    @Override
    public List<ProductBacklog> findAll() {
        return productBacklogRepo.findAll();
    }
}
