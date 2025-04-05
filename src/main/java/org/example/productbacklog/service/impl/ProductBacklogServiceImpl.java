package org.example.productbacklog.service.impl;

import org.example.productbacklog.converter.ProductBacklogConverter;
import org.example.productbacklog.converter.SprintBacklogConverter;
import org.example.productbacklog.dto.ProductBacklogDTO;
import org.example.productbacklog.dto.SprintBacklogDTO;
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
import java.util.Collections;
import java.time.LocalDate;

@Service
public class ProductBacklogServiceImpl implements ProductBacklogService {

    @Autowired
    private ProductBacklogRepository productBacklogRepo;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SprintBacklogRepository sprintBacklogRepository;

    @Autowired
    private ProductBacklogConverter productBacklogConverter;

    @Autowired
    private SprintBacklogConverter sprintBacklogConverter;

    @Override
    @Transactional
    public ProductBacklogDTO addProductBacklog(ProductBacklogDTO productBacklogDTO) {
        // Convert DTO to entity
        ProductBacklog productBacklog = productBacklogConverter.dtoToEntity(productBacklogDTO);

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
            ProductBacklog savedEntity = productBacklogRepo.save(productBacklog);
            return productBacklogConverter.entityToDto(savedEntity);
        }

        ProductBacklog savedEntity = productBacklogRepo.save(productBacklog);
        return productBacklogConverter.entityToDto(savedEntity);
    }

    @Override
    public ProductBacklogDTO findProductBacklogByNom(String nom) {
        Optional<ProductBacklog> backlogOptional = productBacklogRepo.findFirstByTitle(nom);
        if (backlogOptional.isEmpty()) {
            throw new IllegalStateException("ProductBacklog not found");
        }
        return productBacklogConverter.entityToDto(backlogOptional.get());
    }

    @Override
    @Transactional
    public ProductBacklogDTO deleteProductBacklog(Integer id) {
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
            return productBacklogConverter.entityToDto(backlogToDelete);
        }
        throw new IllegalStateException("ProductBacklog not found");
    }

    @Override
    @Transactional
    public ProductBacklogDTO updateProductBacklog(Integer id, ProductBacklogDTO productBacklogDTO) {
        Optional<ProductBacklog> existingBacklogOptional = productBacklogRepo.findById(id);
        if (existingBacklogOptional.isPresent()) {
            ProductBacklog existingBacklog = existingBacklogOptional.get();
            ProductBacklog updatedBacklog = productBacklogConverter.dtoToEntity(productBacklogDTO);

            if (updatedBacklog.getTitle() != null) {
                existingBacklog.setTitle(updatedBacklog.getTitle());
            }

            // Handle project relationship changes
            if (updatedBacklog.getProject() != existingBacklog.getProject()) {
                // First remove the old relationship if it exists
                if (existingBacklog.getProject() != null) {
                    Project oldProject = existingBacklog.getProject();
                    oldProject.setProductBacklog(null);
                    projectRepository.save(oldProject);
                    existingBacklog.setProject(null);
                    existingBacklog = productBacklogRepo.save(existingBacklog);
                }

                // Then establish the new relationship if needed
                if (updatedBacklog.getProject() != null) {
                    Project newProject = updatedBacklog.getProject();
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

            if (updatedBacklog.getEpics() != null && !updatedBacklog.getEpics().isEmpty()) {
                existingBacklog.setEpics(updatedBacklog.getEpics());
            }

            ProductBacklog savedBacklog = productBacklogRepo.save(existingBacklog);
            return productBacklogConverter.entityToDto(savedBacklog);
        }
        throw new IllegalStateException("ProductBacklog not found");
    }

    @Override
    public List<ProductBacklogDTO> findByProjectProjectId(int projectId) {
        List<ProductBacklog> productBacklogs = productBacklogRepo.findByProjectProjectId(projectId);
        return productBacklogConverter.entitiesToDtos(productBacklogs);
    }

    @Override
    public List<ProductBacklogDTO> findAll() {
        List<ProductBacklog> productBacklogs = productBacklogRepo.findAll();
        return productBacklogConverter.entitiesToDtos(productBacklogs);
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
    public List<SprintBacklogDTO> getAllSprintBacklogsByProductBacklog(Integer productBacklogId) {
        Optional<ProductBacklog> productBacklogOptional = productBacklogRepo.findById(productBacklogId);
        if (productBacklogOptional.isPresent()) {
            ProductBacklog productBacklog = productBacklogOptional.get();
            return sprintBacklogConverter.convertToDTOList(productBacklog.getSprintBacklogs());
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
            int totalSprintCount = sprintBacklogs.stream()
                    .mapToInt(sprint -> sprint.getSprints().size())
                    .sum();

            stats.put("totalSprintBacklogs", totalSprints);
            stats.put("totalUserStories", totalUserStories);
            stats.put("totalSprints", totalSprintCount);
        } else {
            stats.put("totalSprintBacklogs", 0);
            stats.put("totalUserStories", 0);
            stats.put("totalSprints", 0);
        }

        return stats;
    }

    @Override
    public List<SprintBacklogDTO> getSprintBacklogsByProductBacklogId(Integer productBacklogId) {
        Optional<ProductBacklog> productBacklogOptional = productBacklogRepo.findById(productBacklogId);
        if (productBacklogOptional.isPresent()) {
            ProductBacklog productBacklog = productBacklogOptional.get();
            List<SprintBacklog> sprintBacklogs = sprintBacklogRepository.findByProductBacklog(productBacklog);
            return sprintBacklogConverter.convertToDTOList(sprintBacklogs);
        }
        throw new RuntimeException("ProductBacklog not found with ID: " + productBacklogId);
    }

    @Override
    public List<SprintBacklogDTO> getActiveSprintBacklogsByProductBacklogId(Integer productBacklogId) {
        Optional<ProductBacklog> productBacklogOptional = productBacklogRepo.findById(productBacklogId);
        if (productBacklogOptional.isPresent()) {
            ProductBacklog productBacklog = productBacklogOptional.get();

            // Get all sprint backlogs for this product backlog
            List<SprintBacklog> allSprintBacklogs = sprintBacklogRepository.findByProductBacklog(productBacklog);

            // For demonstration purposes, we'll consider a sprint backlog "active" if it has at least one sprint
            // that has a start date before or equal to today and an end date after or equal to today
            LocalDate today = LocalDate.now();

            List<SprintBacklog> activeSprintBacklogs = allSprintBacklogs.stream()
                    .filter(sprintBacklog ->
                            sprintBacklog.getSprints().stream().anyMatch(sprint ->
                                    (sprint.getStartDate() == null || !sprint.getStartDate().isAfter(today)) &&
                                            (sprint.getEndDate() == null || !sprint.getEndDate().isBefore(today))
                            )
                    )
                    .toList();

            return sprintBacklogConverter.convertToDTOList(activeSprintBacklogs);
        }
        throw new RuntimeException("ProductBacklog not found with ID: " + productBacklogId);
    }

    @Override
    public List<SprintBacklogDTO> getCompletedSprintBacklogsByProductBacklogId(Integer productBacklogId) {
        Optional<ProductBacklog> productBacklogOptional = productBacklogRepo.findById(productBacklogId);
        if (productBacklogOptional.isPresent()) {
            ProductBacklog productBacklog = productBacklogOptional.get();

            // Get all sprint backlogs for this product backlog
            List<SprintBacklog> allSprintBacklogs = sprintBacklogRepository.findByProductBacklog(productBacklog);

            // For demonstration purposes, we'll consider a sprint backlog "completed" if all of its sprints
            // have an end date that is before today
            LocalDate today = LocalDate.now();

            List<SprintBacklog> completedSprintBacklogs = allSprintBacklogs.stream()
                    .filter(sprintBacklog ->
                            !sprintBacklog.getSprints().isEmpty() &&
                                    sprintBacklog.getSprints().stream().allMatch(sprint ->
                                            sprint.getEndDate() != null && sprint.getEndDate().isBefore(today)
                                    )
                    )
                    .toList();

            return sprintBacklogConverter.convertToDTOList(completedSprintBacklogs);
        }
        throw new RuntimeException("ProductBacklog not found with ID: " + productBacklogId);
    }
}