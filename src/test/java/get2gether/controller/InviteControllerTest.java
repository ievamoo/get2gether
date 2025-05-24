package get2gether.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import get2gether.dto.InviteDto;
import get2gether.enums.Role;
import get2gether.enums.Type;
import get2gether.model.*;
import get2gether.repository.GroupRepository;
import get2gether.repository.InviteRepository;
import get2gether.repository.UserRepository;
import get2gether.security.JwtUtil;
import jakarta.persistence.EntityManager;
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
import org.springframework.transaction.annotation.Transactional;

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
@ActiveProfiles("test")
@Transactional
class InviteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private User adminUser;
    private User invitedUser;
    private Group testGroup;
    private String adminToken;
    private String invitedUserToken;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Create admin user
        adminUser = User.builder()
                .username("admin@test.com")
                .firstName("Admin")
                .lastName("User")
                .password(passwordEncoder.encode("password"))
                .roles(new ArrayList<>(List.of(Role.USER)))
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .availableDays(new HashSet<>())
                .build();
        adminUser = userRepository.save(adminUser);

        // Create invited user
        invitedUser = User.builder()
                .username("invited@test.com")
                .firstName("Invited")
                .lastName("User")
                .password(passwordEncoder.encode("password"))
                .roles(new ArrayList<>(List.of(Role.USER)))
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .availableDays(new HashSet<>())
                .build();
        invitedUser = userRepository.save(invitedUser);

        // Create test group
        Set<User> members = new HashSet<>();
        members.add(adminUser);

        testGroup = Group.builder()
                .name("Test Group")
                .admin(adminUser)
                .members(members)
                .events(new ArrayList<>())
                .messages(new ArrayList<>())
                .build();
        testGroup = groupRepository.save(testGroup);

        // Ensure bidirectional relationship
        adminUser.getGroups().add(testGroup);
        userRepository.save(adminUser);

        // Generate JWT tokens
        UserDetails adminDetails = org.springframework.security.core.userdetails.User
                .withUsername(adminUser.getUsername())
                .password("irrelevant_in_token")
                .authorities("USER")
                .build();
        adminToken = jwtUtil.generateToken(adminDetails);

        UserDetails invitedUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(invitedUser.getUsername())
                .password("irrelevant_in_token")
                .authorities("USER")
                .build();
        invitedUserToken = jwtUtil.generateToken(invitedUserDetails);
    }

    @Test
    void createAndAcceptGroupInvite_shouldSucceed() throws Exception {
        // Create invite
        InviteDto inviteDto = InviteDto.builder()
                .type(Type.GROUP)
                .typeId(testGroup.getId())
                .typeName(testGroup.getName())
                .groupName(testGroup.getName())
                .eventDate(LocalDate.now())
                .senderUsername(adminUser.getUsername())
                .receiverUsernames(new HashSet<>(Set.of(invitedUser.getUsername())))
                .build();

        mockMvc.perform(post("/invites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));

        // Force refresh from database
        entityManager.flush();
        entityManager.clear();

        // Refresh user from database
        invitedUser = userRepository.findByUsername(invitedUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify invite was created and get its ID
        Invite createdInvite = inviteRepository.findByReceiverAndTypeAndTypeId(invitedUser, Type.GROUP, testGroup.getId())
                .orElseThrow(() -> new RuntimeException("Invite not found after creation"));
        assertNotNull(createdInvite.getId(), "Invite ID should not be null");

        // Verify invite is in user's received invites
        assertTrue(invitedUser.getInvitesReceived().contains(createdInvite), "Invite should be in user's received invites");

        // Accept invite
        InviteDto responseDto = InviteDto.builder()
                .accepted(true)
                .build();

        mockMvc.perform(patch("/invites/" + createdInvite.getId())
                        .header("Authorization", "Bearer " + invitedUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(responseDto)))
                .andExpect(status().isOk());

        // Force refresh from database
        entityManager.flush();
        entityManager.clear();

        // Refresh user from database
        invitedUser = userRepository.findByUsername(invitedUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify invite was removed from user's received invites
        assertFalse(invitedUser.getInvitesReceived().contains(createdInvite), "Invite should be removed from user's received invites");
        
        // Verify invite was deleted from repository
        assertFalse(inviteRepository.existsById(createdInvite.getId()), "Invite should be deleted from repository");

        // Verify user was added to group
        assertTrue(groupRepository.findById(testGroup.getId()).get().getMembers().contains(invitedUser), "User should be added to group");
    }

    @Test
    void createAndDeclineGroupInvite_shouldSucceed() throws Exception {
        // Create invite
        InviteDto inviteDto = InviteDto.builder()
                .type(Type.GROUP)
                .typeId(testGroup.getId())
                .typeName(testGroup.getName())
                .groupName(testGroup.getName())
                .eventDate(LocalDate.now())
                .senderUsername(adminUser.getUsername())
                .receiverUsernames(new HashSet<>(Set.of(invitedUser.getUsername())))
                .build();

        mockMvc.perform(post("/invites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));

        // Force refresh from database
        entityManager.flush();
        entityManager.clear();

        // Refresh user from database
        invitedUser = userRepository.findByUsername(invitedUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify invite was created and get its ID
        Invite createdInvite = inviteRepository.findByReceiverAndTypeAndTypeId(invitedUser, Type.GROUP, testGroup.getId())
                .orElseThrow(() -> new RuntimeException("Invite not found after creation"));
        assertNotNull(createdInvite.getId(), "Invite ID should not be null");

        // Verify invite is in user's received invites
        assertTrue(invitedUser.getInvitesReceived().contains(createdInvite), "Invite should be in user's received invites");

        // Decline invite
        InviteDto responseDto = InviteDto.builder()
                .accepted(false)
                .build();

        mockMvc.perform(patch("/invites/" + createdInvite.getId())
                        .header("Authorization", "Bearer " + invitedUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(responseDto)))
                .andExpect(status().isOk());

        // Force refresh from database
        entityManager.flush();
        entityManager.clear();

        // Refresh user from database
        invitedUser = userRepository.findByUsername(invitedUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify invite was removed from user's received invites
        assertFalse(invitedUser.getInvitesReceived().contains(createdInvite), "Invite should be removed from user's received invites");
        
        // Verify invite was deleted from repository
        assertFalse(inviteRepository.existsById(createdInvite.getId()), "Invite should be deleted from repository");

        // Verify user was not added to group
        assertFalse(groupRepository.findById(testGroup.getId()).get().getMembers().contains(invitedUser), "User should not be added to group");
    }

    @Test
    void createInvite_withoutPermission_shouldFail() throws Exception {
        // Try to create invite as invited user (who is not admin)
        InviteDto inviteDto = InviteDto.builder()
                .type(Type.GROUP)
                .typeId(testGroup.getId())
                .typeName(testGroup.getName())
                .groupName(testGroup.getName())
                .eventDate(LocalDate.now())
                .senderUsername(invitedUser.getUsername())
                .receiverUsernames(new HashSet<>(Set.of("someother@test.com")))
                .build();

        mockMvc.perform(post("/invites")
                        .header("Authorization", "Bearer " + invitedUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteDto)))
                .andExpect(status().isForbidden());
    }
}
