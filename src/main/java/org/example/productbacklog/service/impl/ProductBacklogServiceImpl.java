package org.example.productbacklog.service.impl;

import org.example.productbacklog.entity.ProductBacklog;
import org.example.productbacklog.entity.Project;
import org.example.productbacklog.entity.SprintBacklog;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.repository.ProjectRepository;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.example.productbacklog.service.ProductBacklogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductBacklogServiceImpl implements ProductBacklogService {

    @Autowired
    private ProductBacklogRepository productBacklogRepo;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SprintBacklogRepository sprintBacklogRepository;

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

    @Override
    @Transactional
    public boolean addSprintBacklogToProductBacklog(Integer productBacklogId, Long sprintBacklogId) {
        Optional<ProductBacklog> productBacklogOptional = productBacklogRepo.findById(productBacklogId);
        Optional<SprintBacklog> sprintBacklogOptional = sprintBacklogRepository.findById(sprintBacklogId);

        if (productBacklogOptional.isPresent() && sprintBacklogOptional.isPresent()) {
            ProductBacklog productBacklog = productBacklogOptional.get();
            SprintBacklog sprintBacklog = sprintBacklogOptional.get();

            // Check if sprint backlog is already associated with another product backlog
            if (sprintBacklog.getProductBacklog() != null &&
                    !sprintBacklog.getProductBacklog().getId().equals(productBacklogId)) {
                // Remove from previous product backlog
                ProductBacklog oldProductBacklog = sprintBacklog.getProductBacklog();
                oldProductBacklog.getSprintBacklogs().remove(sprintBacklog);
                productBacklogRepo.save(oldProductBacklog);
            }

            // Set the product backlog for this sprint backlog
            sprintBacklog.setProductBacklog(productBacklog);

            // Add the sprint backlog to the product backlog's list if not already present
            if (!productBacklog.getSprintBacklogs().contains(sprintBacklog)) {
                productBacklog.getSprintBacklogs().add(sprintBacklog);
            }

            sprintBacklogRepository.save(sprintBacklog);
            productBacklogRepo.save(productBacklog);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean removeSprintBacklogFromProductBacklog(Integer productBacklogId, Long sprintBacklogId) {
        Optional<ProductBacklog> productBacklogOptional = productBacklogRepo.findById(productBacklogId);
        Optional<SprintBacklog> sprintBacklogOptional = sprintBacklogRepository.findById(sprintBacklogId);

        if (productBacklogOptional.isPresent() && sprintBacklogOptional.isPresent()) {
            ProductBacklog productBacklog = productBacklogOptional.get();
            SprintBacklog sprintBacklog = sprintBacklogOptional.get();

            // Check if the sprint backlog belongs to this product backlog
            if (sprintBacklog.getProductBacklog() != null &&
                    sprintBacklog.getProductBacklog().getId().equals(productBacklogId)) {
                // Remove the association
                sprintBacklog.setProductBacklog(null);
                productBacklog.getSprintBacklogs().remove(sprintBacklog);

                sprintBacklogRepository.save(sprintBacklog);
                productBacklogRepo.save(productBacklog);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<SprintBacklog> getAllSprintBacklogsByProductBacklog(Integer productBacklogId) {
        Optional<ProductBacklog> productBacklogOptional = productBacklogRepo.findById(productBacklogId);
        if (productBacklogOptional.isPresent()) {
            ProductBacklog productBacklog = productBacklogOptional.get();
            return productBacklog.getSprintBacklogs();
        }
        throw new EntityNotFoundException("ProductBacklog not found with ID: " + productBacklogId);
    }

    @Override
    public Map<String, Integer> getProductBacklogSprintStatistics(Integer productBacklogId) {
        Optional<ProductBacklog> productBacklogOptional = productBacklogRepo.findById(productBacklogId);
        Map<String, Integer> stats = new HashMap<>();

        if (productBacklogOptional.isPresent()) {
            ProductBacklog productBacklog = productBacklogOptional.get();
            List<SprintBacklog> sprintBacklogs = productBacklog.getSprintBacklogs();

            int totalSprints = sprintBacklogs.size();
            int totalUserStories = sprintBacklogs.stream()
                    .mapToInt(sprint -> sprint.getUserStories().size())
                    .sum();
            totalSprints = sprintBacklogs.stream()
                    .mapToInt(sprint -> sprint.getSprints().size())
                    .sum();

            stats.put("totalSprintBacklogs", totalSprints);
            stats.put("totalUserStories", totalUserStories);
            stats.put("totalSprints", totalSprints);
        } else {
            stats.put("totalSprintBacklogs", 0);
            stats.put("totalUserStories", 0);
            stats.put("totalSprints", 0);
        }

        return stats;
    }
}