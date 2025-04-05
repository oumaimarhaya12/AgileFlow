package org.example.test;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.example.productbacklog.converter.UserStoryConverter;
import org.example.productbacklog.dto.UserStoryDTO;
import org.example.productbacklog.entity.*;
import org.example.productbacklog.repository.EpicRepository;
import org.example.productbacklog.repository.ProductBacklogRepository;
import org.example.productbacklog.repository.SprintBacklogRepository;
import org.example.productbacklog.repository.UserStoryRepository;
import org.example.productbacklog.service.impl.UserStoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserStoryServiceTest {

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private SprintBacklogRepository sprintBacklogRepository;

    @Mock
    private ProductBacklogRepository productBacklogRepository;

    @Spy
    private UserStoryConverter userStoryConverter;

    @InjectMocks
    private UserStoryServiceImpl userStoryService;

    private UserStory userStory;
    private UserStoryDTO userStoryDTO;
    private Epic epic;
    private SprintBacklog sprintBacklog;
    private ProductBacklog productBacklog;
    private List<UserStory> userStoryList;
    private List<UserStoryDTO> userStoryDTOList;

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

        // Initialize UserStoryDTO
        userStoryDTO = new UserStoryDTO();
        userStoryDTO.setTitle("Test User Story");
        userStoryDTO.setAsA("user");
        userStoryDTO.setIWant("to test");
        userStoryDTO.setSoThat("I can validate");
        userStoryDTO.setDescription("Test description");
        userStoryDTO.setAcceptanceCriteria("Test acceptance criteria");
        userStoryDTO.setPriority(1);
        userStoryDTO.setStatus(Statut.toDo.name());
        userStoryDTO.setEpicId(1);
        userStoryDTO.setProductBacklogId(1);
        userStoryDTO.setSprintBacklogId(1L);

        // Initialize list of user stories
        userStoryList = new ArrayList<>();
        userStoryList.add(userStory);

        // Initialize list of user story DTOs
        userStoryDTOList = new ArrayList<>();
        userStoryDTOList.add(userStoryDTO);

        // Setup the spy converter
        doReturn(userStoryDTO).when(userStoryConverter).convertToDTO(userStory);
        doReturn(userStory).when(userStoryConverter).convertToEntity(userStoryDTO);
        doReturn(userStoryDTOList).when(userStoryConverter).convertToDTOList(userStoryList);
    }

    // CRUD operation tests
    @Test
    public void testSaveUserStory() {
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(userStory);

        UserStoryDTO savedUserStory = userStoryService.saveUserStory(userStoryDTO);

        assertNotNull(savedUserStory);
        assertEquals(userStoryDTO.getTitle(), savedUserStory.getTitle());
        verify(userStoryRepository, times(1)).save(any(UserStory.class));
    }

    @Test
    public void testGetUserStoryById() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.of(userStory));

        Optional<UserStoryDTO> result = userStoryService.getUserStoryById(1L);

        assertTrue(result.isPresent());
        assertEquals(userStoryDTO.getTitle(), result.get().getTitle());
        verify(userStoryRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAllUserStories() {
        when(userStoryRepository.findAll()).thenReturn(userStoryList);

        List<UserStoryDTO> result = userStoryService.getAllUserStories();

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

        UserStoryDTO result = userStoryService.linkUserStoryToEpic(1L, 1);

        assertNotNull(result);
        assertEquals(userStoryDTO.getTitle(), result.getTitle());
        verify(userStoryRepository, times(1)).findById(1L);
        verify(epicRepository, times(1)).findById(1L);
        verify(userStoryRepository, times(1)).save(userStory);
    }

    @Test
    public void testGetUserStoriesByEpic() {
        when(userStoryRepository.findByEpicId(anyInt())).thenReturn(userStoryList);

        List<UserStoryDTO> result = userStoryService.getUserStoriesByEpic(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findByEpicId(1);
    }

    @Test
    public void testGetUserStoriesWithoutEpic() {
        when(userStoryRepository.findByEpicIsNull()).thenReturn(userStoryList);

        List<UserStoryDTO> result = userStoryService.getUserStoriesWithoutEpic();

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

        UserStoryDTO result = userStoryService.addUserStoryToSprintBacklog(1L, 1L);

        assertNotNull(result);
        assertEquals(userStoryDTO.getTitle(), result.getTitle());
        verify(userStoryRepository, times(1)).findById(1L);
        verify(sprintBacklogRepository, times(1)).findById(1L);
        verify(userStoryRepository, times(1)).save(userStory);
    }

    @Test
    public void testGetUserStoriesBySprintBacklog() {
        when(userStoryRepository.findBySprintBacklogId(anyLong())).thenReturn(userStoryList);

        List<UserStoryDTO> result = userStoryService.getUserStoriesBySprintBacklog(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findBySprintBacklogId(1L);
    }

    // Product Backlog operations tests
    @Test
    public void testGetUserStoriesByProductBacklog() {
        when(userStoryRepository.findByProductBacklogId(anyLong())).thenReturn(userStoryList);

        List<UserStoryDTO> result = userStoryService.getUserStoriesByProductBacklog(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findByProductBacklogId(1L);
    }

    @Test
    public void testGetPrioritizedUserStories() {
        when(userStoryRepository.findByProductBacklogIdOrderByPriorityDesc(anyLong())).thenReturn(userStoryList);

        List<UserStoryDTO> result = userStoryService.getPrioritizedUserStories(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStoryRepository, times(1)).findByProductBacklogIdOrderByPriorityDesc(1L);
    }

    @Test
    public void testUpdateUserStoryPriority() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.of(userStory));

        // Create an ArgumentCaptor to capture the UserStory passed to save()
        ArgumentCaptor<UserStory> userStoryCaptor = ArgumentCaptor.forClass(UserStory.class);
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(userStory);

        // Execute the method
        UserStoryDTO result = userStoryService.updateUserStoryPriority(1L, 5);

        // Verify save was called and capture the argument
        verify(userStoryRepository).save(userStoryCaptor.capture());

        // Assert that the priority was set correctly on the captured UserStory
        assertEquals(5, userStoryCaptor.getValue().getPriority());
        verify(userStoryRepository).findById(1L);
    }

    // Acceptance criteria operations tests
    @Test
    public void testUpdateAcceptanceCriteria() {
        when(userStoryRepository.findById(anyLong())).thenReturn(Optional.of(userStory));
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(userStory);

        String newCriteria = "Updated acceptance criteria";
        UserStoryDTO result = userStoryService.updateAcceptanceCriteria(1L, newCriteria);

        assertNotNull(result);
        verify(userStoryRepository, times(1)).findById(1L);
        verify(userStoryRepository, times(1)).save(userStory);
    }

    @Test
    public void testGetUserStoriesWithoutAcceptanceCriteria() {
        when(userStoryRepository.findWithoutAcceptanceCriteria()).thenReturn(userStoryList);

        List<UserStoryDTO> result = userStoryService.getUserStoriesWithoutAcceptanceCriteria();

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