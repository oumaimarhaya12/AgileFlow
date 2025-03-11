package org.example.test;

import org.example.productbacklog.entity.*;
import org.example.productbacklog.repository.EpicRepository;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.service.impl.UserStoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserStoryServiceTest {

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private SprintBacklogRepository sprintBacklogRepository;

    @Mock
    private ProductBacklogRepository productBacklogRepository;

    @InjectMocks
    private UserStoryServiceImpl userStoryService;

    private UserStory userStory;
    private Epic epic;
    private SprintBacklog sprintBacklog;
    private ProductBacklog productBacklog;
    private List<UserStory> userStoryList;

    @BeforeEach
    public void setup() {
        // Initialize ProductBacklog
        productBacklog = new ProductBacklog();
        productBacklog.setId(1);
        productBacklog.setTitle("Test Product Backlog");

        // Initialize Epic
        epic = new Epic();
        epic.setId(1);
        epic.setTitle("Test Epic");
        epic.setProductBacklog(productBacklog);

        // Initialize SprintBacklog
        sprintBacklog = new SprintBacklog();
        sprintBacklog.setId(1L);
        sprintBacklog.setTitle("Test Sprint Backlog");

        // Initialize UserStory
        userStory = UserStory.builder()
                .id(1L)
                .title("Test User Story")
                .asA("user")
                .iWant("to test")
                .soThat("I can validate")
                .description("Test description")
                .acceptanceCriteria("Test acceptance criteria")
                .priority(1)
                .status(Statut.toDo)
                .build();

        // Initialize list of user stories
        userStoryList = new ArrayList<>();
        userStoryList.add(userStory);
    }

    // CRUD operation tests
    @Test
    public void testSaveUserStory() {
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(userStory);

        UserStory savedUserStory = userStoryService.saveUserStory(userStory);

        assertNotNull(savedUserStory);
        assertEquals(userStory.getId(), savedUserStory.getId());
        verify(userStoryRepository, times(1)).save(userStory);
    }

    @Test
    public void testGetUserStoryById() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.of(userStory));

        Optional<UserStory> result = userStoryService.getUserStoryById(1L);

        assertTrue(result.isPresent());
        assertEquals(userStory.getId(), result.get().getId());
        verify(userStoryRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAllUserStories() {
        when(userStoryRepository.findAll()).thenReturn(userStoryList);

        List<UserStory> result = userStoryService.getAllUserStories();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findAll();
    }

    @Test
    public void testDeleteUserStory() {
        doNothing().when(userStoryRepository).deleteById(anyLong());

        userStoryService.deleteUserStory(1L);

        verify(userStoryRepository, times(1)).deleteById(1L);
    }

    // Epic-related operations tests
    @Test
    public void testLinkUserStoryToEpic() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.of(userStory));
        when(epicRepository.findById(eq(1L))).thenReturn(Optional.of(epic));
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(userStory);

        UserStory result = userStoryService.linkUserStoryToEpic(1L, 1);

        assertNotNull(result);
        assertEquals(epic, result.getEpic());
        verify(userStoryRepository, times(1)).findById(1L);
        verify(epicRepository, times(1)).findById(1L);
        verify(userStoryRepository, times(1)).save(userStory);
    }

    @Test
    public void testGetUserStoriesByEpic() {
        when(userStoryRepository.findByEpicId(anyInt())).thenReturn(userStoryList);

        List<UserStory> result = userStoryService.getUserStoriesByEpic(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findByEpicId(1);
    }

    @Test
    public void testGetUserStoriesWithoutEpic() {
        when(userStoryRepository.findByEpicIsNull()).thenReturn(userStoryList);

        List<UserStory> result = userStoryService.getUserStoriesWithoutEpic();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findByEpicIsNull();
    }

    // Sprint Backlog operations tests
    @Test
    public void testAddUserStoryToSprintBacklog() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.of(userStory));
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.of(sprintBacklog));
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(userStory);

        UserStory result = userStoryService.addUserStoryToSprintBacklog(1L, 1L);

        assertNotNull(result);
        assertEquals(sprintBacklog, result.getSprintBacklog());
        verify(userStoryRepository, times(1)).findById(1L);
        verify(sprintBacklogRepository, times(1)).findById(1L);
        verify(userStoryRepository, times(1)).save(userStory);
    }

    @Test
    public void testGetUserStoriesBySprintBacklog() {
        when(userStoryRepository.findBySprintBacklogId(anyLong())).thenReturn(userStoryList);

        List<UserStory> result = userStoryService.getUserStoriesBySprintBacklog(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findBySprintBacklogId(1L);
    }

    // Product Backlog operations tests
    @Test
    public void testGetUserStoriesByProductBacklog() {
        when(userStoryRepository.findByProductBacklogId(anyLong())).thenReturn(userStoryList);

        List<UserStory> result = userStoryService.getUserStoriesByProductBacklog(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findByProductBacklogId(1L);
    }

    @Test
    public void testGetPrioritizedUserStories() {
        when(userStoryRepository.findByProductBacklogIdOrderByPriorityDesc(anyLong())).thenReturn(userStoryList);

        List<UserStory> result = userStoryService.getPrioritizedUserStories(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findByProductBacklogIdOrderByPriorityDesc(1L);
    }

    @Test
    public void testUpdateUserStoryPriority() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.of(userStory));
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(userStory);

        UserStory result = userStoryService.updateUserStoryPriority(1L, 2);

        assertNotNull(result);
        assertEquals(2, result.getPriority());
        verify(userStoryRepository, times(1)).findById(1L);
        verify(userStoryRepository, times(1)).save(userStory);
    }

    // Acceptance criteria operations tests
    @Test
    public void testUpdateAcceptanceCriteria() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.of(userStory));
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(userStory);

        String newCriteria = "Updated acceptance criteria";
        UserStory result = userStoryService.updateAcceptanceCriteria(1L, newCriteria);

        assertNotNull(result);
        assertEquals(newCriteria, result.getAcceptanceCriteria());
        verify(userStoryRepository, times(1)).findById(1L);
        verify(userStoryRepository, times(1)).save(userStory);
    }

    @Test
    public void testGetUserStoriesWithoutAcceptanceCriteria() {
        when(userStoryRepository.findWithoutAcceptanceCriteria()).thenReturn(userStoryList);

        List<UserStory> result = userStoryService.getUserStoriesWithoutAcceptanceCriteria();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findWithoutAcceptanceCriteria();
    }

    // Exception test cases
    @Test
    public void testLinkUserStoryToEpic_UserStoryNotFound() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userStoryService.linkUserStoryToEpic(1L, 1));
        verify(userStoryRepository, times(1)).findById(1L);
        verify(epicRepository, never()).findById((long) anyInt());
    }

    @Test
    public void testLinkUserStoryToEpic_EpicNotFound() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.of(userStory));
        when(epicRepository.findById(eq(1L))).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userStoryService.linkUserStoryToEpic(1L, 1));
        verify(userStoryRepository, times(1)).findById(1L);
        verify(epicRepository, times(1)).findById(1L);
        verify(userStoryRepository, never()).save(any(UserStory.class));
    }

    @Test
    public void testAddUserStoryToSprintBacklog_UserStoryNotFound() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userStoryService.addUserStoryToSprintBacklog(1L, 1L));
        verify(userStoryRepository, times(1)).findById(1L);
        verify(sprintBacklogRepository, never()).findById(anyLong());
    }

    @Test
    public void testAddUserStoryToSprintBacklog_SprintBacklogNotFound() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.of(userStory));
        when(sprintBacklogRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userStoryService.addUserStoryToSprintBacklog(1L, 1L));
        verify(userStoryRepository, times(1)).findById(1L);
        verify(sprintBacklogRepository, times(1)).findById(1L);
        verify(userStoryRepository, never()).save(any(UserStory.class));
    }
}