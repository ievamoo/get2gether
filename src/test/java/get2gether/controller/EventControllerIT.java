package get2gether.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import get2gether.TestData;
import get2gether.dto.EventDto;
import get2gether.model.User;
import get2gether.model.Group;
import get2gether.repository.EventRepository;
import get2gether.repository.GroupRepository;
import get2gether.repository.UserRepository;
import get2gether.security.JwtUtil;
import get2gether.service.EventService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestData.getTestUser();
        var testGroup = TestData.getGroupWithoutId();

        testUser.setGroups(Set.of(testGroup));
        userRepository.save(testUser);
        groupRepository.save(testGroup);

        UserDetails testUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(testUser.getUsername())
                .password("irrelevant")
                .authorities("USER")
                .build();

        token = jwtUtil.generateToken(testUserDetails);
    }

    @Test
    void createEvent() throws Exception {
        var eventDto = TestData.getEventDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Liverpool's parade"))
                .andExpect(jsonPath("$.description").value(eventDto.getDescription()))
                .andExpect(jsonPath("$.hostUsername").value("test@gmail.com"));
    }

    @Test
    void editEvent_shouldUpdateNameAndDescription_whenUserIsHost() throws Exception {
        var event = TestData.getEvent();
        event.setHostUsername(testUser.getUsername());
        event = eventRepository.save(event);

        var updateDto = EventDto.builder()
                .name("Updated Event Name")
                .description("New description")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch("/events/{eventId}", event.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isAccepted());
    }

    @Test
    void editEvent_shouldReturnForbidden_whenUserIsNotHost() throws Exception {
        String otherUserToken = createNonHostUserAndGetToken();

        var event = TestData.getEvent();
        event.setHostUsername(testUser.getUsername());
        eventRepository.save(event);

        var updateDto = EventDto.builder()
                .name("Updated Event Name")
                .description("New description")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch("/events/{eventId}", event.getId())
                        .header("Authorization", "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void toggleEventAttendance_shouldReturnAccepted_whenUserTogglesAttendance() throws Exception {
        var event = TestData.getEvent();
        event.setHostUsername(testUser.getUsername());
        event.setGoingMembers(new HashSet<>(Set.of(testUser)));  // Use mutable HashSet
        eventRepository.save(event);

        var status = true;
        mockMvc.perform(MockMvcRequestBuilders.patch("/events/{eventId}/status", event.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isAccepted());
    }

    @Test
    void deleteEvent_shouldReturnNoContent_whenHostDeletesOwnEvent() throws Exception {
        var event = TestData.getEvent();
        event.setHostUsername(testUser.getUsername());
        var testGroup = Group.builder()
                .name("Test Group " + System.currentTimeMillis())  // Make name unique
                .members(Set.of(testUser))
                .admin(testUser)
                .build();
        testGroup = groupRepository.save(testGroup);
        event.setGroup(testGroup);
        event = eventRepository.save(event);
        mockMvc.perform(MockMvcRequestBuilders.delete("/events/{eventId}", event.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEvent_shouldReturnNotFound_whenEventDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/events/{eventId}", 999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEvent_shouldReturnForbidden_whenUserIsNotHost() throws Exception {
        String otherUserToken = createNonHostUserAndGetToken();

        var event = TestData.getEvent();
        event.setHostUsername(testUser.getUsername());
        eventRepository.save(event);

        mockMvc.perform(MockMvcRequestBuilders.delete("/events/{eventId}", event.getId())
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    private String createNonHostUserAndGetToken() {
        var otherUser = TestData.getNotHostUser();
        userRepository.save(otherUser);

        UserDetails otherUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(otherUser.getUsername())
                .password("irrelevant")
                .authorities("USER")
                .build();

        return jwtUtil.generateToken(otherUserDetails);
    }
}
