package get2gether.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import get2gether.dto.InviteDto;
import get2gether.model.*;
import get2gether.repository.GroupRepository;
import get2gether.repository.InviteRepository;
import get2gether.repository.UserRepository;
import get2gether.service.InviteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class InviteControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User user1;
    private User user2;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Configure ObjectMapper
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Create test users
        testUser = User.builder()
                .username("testuser@test.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .roles(new ArrayList<>(List.of(Role.USER)))
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .availableDays(new HashSet<>())
                .build();
        userRepository.save(testUser);

        user1 = User.builder()
                .username("user1@test.com")
                .firstName("User")
                .lastName("One")
                .password("password")
                .roles(new ArrayList<>(List.of(Role.USER)))
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .availableDays(new HashSet<>())
                .build();
        userRepository.save(user1);

        user2 = User.builder()
                .username("user2@test.com")
                .firstName("User")
                .lastName("Two")
                .password("password")
                .roles(new ArrayList<>(List.of(Role.USER)))
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .availableDays(new HashSet<>())
                .build();
        userRepository.save(user2);

        // Create test group with mutable collections
        Set<User> members = new HashSet<>();
        members.add(testUser);

        testGroup = Group.builder()
                .name("Test Group")
                .admin(testUser)
                .members(members)
                .events(new ArrayList<>())
                .messages(new ArrayList<>())
                .build();
        groupRepository.save(testGroup);
        testUser.getGroups().add(testGroup);
        userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = "testuser@test.com")
    void createGroupInvite_shouldReturnCreated() throws Exception {
        InviteDto dto = InviteDto.builder()
                .type(Type.GROUP)
                .typeId(testGroup.getId())
                .typeName(testGroup.getName())
                .groupName(testGroup.getName())
                .eventDate(LocalDate.now())
                .senderUsername(testUser.getUsername())
                .receiverUsernames(new HashSet<>(Set.of(user1.getUsername(), user2.getUsername())))
                .build();

        mockMvc.perform(post("/invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));

        // Verify invites were created
        assertTrue(inviteRepository.existsByReceiverAndTypeAndTypeId(user1, Type.GROUP, testGroup.getId()));
        assertTrue(inviteRepository.existsByReceiverAndTypeAndTypeId(user2, Type.GROUP, testGroup.getId()));
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void respondToInvite_shouldReturnOk() throws Exception {
        // Create an invite first
        Invite invite = Invite.builder()
                .type(Type.GROUP)
                .typeId(testGroup.getId())
                .typeName(testGroup.getName())
                .senderUsername(testUser.getUsername())
                .receiver(user1)
                .build();
        user1.getInvitesReceived().add(invite);
        inviteRepository.save(invite);

        InviteDto responseDto = InviteDto.builder()
                .accepted(true)
                .build();

        mockMvc.perform(patch("/invites/" + invite.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(responseDto)))
                .andExpect(status().isOk());

        // Verify invite was processed
        assertFalse(inviteRepository.existsById(invite.getId()));
        assertTrue(groupRepository.findById(testGroup.getId()).get().getMembers().contains(user1));
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void respondToInvite_withDecline_shouldReturnOk() throws Exception {
        // Create an invite first
        Invite invite = Invite.builder()
                .type(Type.GROUP)
                .typeId(testGroup.getId())
                .typeName(testGroup.getName())
                .senderUsername(testUser.getUsername())
                .receiver(user1)
                .build();
        user1.getInvitesReceived().add(invite);
        inviteRepository.save(invite);

        InviteDto responseDto = InviteDto.builder()
                .accepted(false)
                .build();

        mockMvc.perform(patch("/invites/" + invite.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(responseDto)))
                .andExpect(status().isOk());

        // Verify invite was processed but user not added to group
        assertFalse(inviteRepository.existsById(invite.getId()));
        assertFalse(groupRepository.findById(testGroup.getId()).get().getMembers().contains(user1));
    }
}
