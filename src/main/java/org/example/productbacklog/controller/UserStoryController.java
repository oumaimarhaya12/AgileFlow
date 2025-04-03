package org.example.productbacklog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.productbacklog.entity.UserStory;
import org.example.productbacklog.service.UserStoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/userstories")
@Tag(name = "User-Stories-Controller", description = "API for managing User Stories")
public class UserStoryController {

    private final UserStoryService userStoryService;

    public UserStoryController(UserStoryService userStoryService) {
        this.userStoryService = userStoryService;
    }

    @PostMapping
    @Operation(summary = "Create a new User Story")
    public ResponseEntity<UserStory> createUserStory(@RequestBody UserStory userStory) {
        return ResponseEntity.ok(userStoryService.saveUserStory(userStory));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a User Story by ID")
    public ResponseEntity<UserStory> getUserStoryById(@PathVariable Long id) {
        Optional<UserStory> userStory = userStoryService.getUserStoryById(id);
        return userStory.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all User Stories")
    public ResponseEntity<List<UserStory>> getAllUserStories() {
        return ResponseEntity.ok(userStoryService.getAllUserStories());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a User Story")
    public ResponseEntity<Void> deleteUserStory(@PathVariable Long id) {
        userStoryService.deleteUserStory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userStoryId}/epic/{epicId}")
    @Operation(summary = "Link a User Story to an Epic")
    public ResponseEntity<UserStory> linkUserStoryToEpic(@PathVariable Long userStoryId, @PathVariable Integer epicId) {
        return ResponseEntity.ok(userStoryService.linkUserStoryToEpic(userStoryId, epicId));
    }

    @GetMapping("/epic/{epicId}")
    @Operation(summary = "Get User Stories by Epic")
    public ResponseEntity<List<UserStory>> getUserStoriesByEpic(@PathVariable Integer epicId) {
        return ResponseEntity.ok(userStoryService.getUserStoriesByEpic(epicId));
    }

    @PostMapping("/{userStoryId}/sprint/{sprintBacklogId}")
    @Operation(summary = "Add a User Story to a Sprint Backlog")
    public ResponseEntity<UserStory> addUserStoryToSprintBacklog(@PathVariable Long userStoryId, @PathVariable Long sprintBacklogId) {
        return ResponseEntity.ok(userStoryService.addUserStoryToSprintBacklog(userStoryId, sprintBacklogId));
    }

    @GetMapping("/sprint/{sprintBacklogId}")
    @Operation(summary = "Get User Stories by Sprint Backlog")
    public ResponseEntity<List<UserStory>> getUserStoriesBySprintBacklog(@PathVariable Long sprintBacklogId) {
        return ResponseEntity.ok(userStoryService.getUserStoriesBySprintBacklog(sprintBacklogId));
    }

    @PatchMapping("/{userStoryId}/priority/{newPriority}")
    @Operation(summary = "Update the priority of a User Story")
    public ResponseEntity<UserStory> updateUserStoryPriority(@PathVariable Long userStoryId, @PathVariable int newPriority) {
        return ResponseEntity.ok(userStoryService.updateUserStoryPriority(userStoryId, newPriority));
    }

    @PatchMapping("/{userStoryId}/criteria")
    @Operation(summary = "Update the acceptance criteria of a User Story")
    public ResponseEntity<UserStory> updateAcceptanceCriteria(@PathVariable Long userStoryId, @RequestBody String acceptanceCriteria) {
        return ResponseEntity.ok(userStoryService.updateAcceptanceCriteria(userStoryId, acceptanceCriteria));
    }
}
