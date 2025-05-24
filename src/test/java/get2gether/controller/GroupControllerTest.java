package get2gether.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.enums.Role;
import get2gether.model.Group;
import get2gether.model.User;
import get2gether.repository.GroupRepository;
import get2gether.repository.UserRepository;
import get2gether.security.JwtUtil;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User regularUser;
    private Group testGroup;
    private String adminToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        groupRepository.deleteAll();

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

        // Create regular user
        regularUser = User.builder()
                .username("user@test.com")
                .firstName("Regular")
                .lastName("User")
                .password(passwordEncoder.encode("password"))
                .roles(new ArrayList<>(List.of(Role.USER)))
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .availableDays(new HashSet<>())
                .build();
        regularUser = userRepository.save(regularUser);

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

        UserDetails regularUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(regularUser.getUsername())
                .password("irrelevant_in_token")
                .authorities("USER")
                .build();
        regularUserToken = jwtUtil.generateToken(regularUserDetails);
    }

    @Test
    void createGroup_shouldSucceed_whenValidInput() throws Exception {
        var groupDto = GroupDto.builder()
                .name("New Group")
                .groupColor("#FF0000")
                .invitedUsernames(Set.of(regularUser.getUsername()))
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/groups")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Group"))
                .andExpect(jsonPath("$.groupColor").value("#FF0000"));
    }

    @Test
    void createGroup_shouldFail_whenNameExists() throws Exception {
        var groupDto = GroupDto.builder()
                .name("Test Group") // Using existing group name
                .groupColor("#FF0000")
                .invitedUsernames(Set.of(regularUser.getUsername()))
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/groups")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGroupById_shouldSucceed_whenGroupExists() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/groups/" + testGroup.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Group"))
                .andExpect(jsonPath("$.admin.username").value(adminUser.getUsername()));
    }

    @Test
    void getGroupById_shouldFail_whenGroupNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/groups/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void editGroup_shouldSucceed_whenAdmin() throws Exception {
        var editedGroup = GroupDto.builder()
                .name("Updated Group Name")
                .groupColor("#00FF00")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/groups/" + testGroup.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editedGroup)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.name").value("Updated Group Name"))
                .andExpect(jsonPath("$.groupColor").value("#00FF00"));
    }

    @Test
    void editGroup_shouldFail_whenNotAdmin() throws Exception {
        var editedGroup = GroupDto.builder()
                .name("Updated Group Name")
                .groupColor("#00FF00")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/groups/" + testGroup.getId())
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editedGroup)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteGroup_shouldSucceed_whenAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/groups/" + testGroup.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertFalse(groupRepository.existsById(testGroup.getId()));
    }

    @Test
    void deleteGroup_shouldFail_whenNotAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/groups/" + testGroup.getId())
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());

        assertTrue(groupRepository.existsById(testGroup.getId()));
    }

    @Test
    void removeUserFromGroup_shouldSucceed_whenAdmin() throws Exception {
        // First add the regular user to the group
        testGroup.getMembers().add(regularUser);
        regularUser.getGroups().add(testGroup);
        groupRepository.save(testGroup);
        userRepository.save(regularUser);

        mockMvc.perform(MockMvcRequestBuilders.delete("/groups/" + testGroup.getId() + "/members/" + regularUser.getUsername())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isAccepted());

        // Verify user was removed
        var updatedGroup = groupRepository.findById(testGroup.getId()).orElseThrow();
        assertFalse(updatedGroup.getMembers().contains(regularUser));
    }

    @Test
    void removeUserFromGroup_shouldFail_whenNotAdmin() throws Exception {
        // Create another user to be removed
        User anotherUser = User.builder()
                .username("another@test.com")
                .firstName("Another")
                .lastName("User")
                .password(passwordEncoder.encode("password"))
                .roles(new ArrayList<>(List.of(Role.USER)))
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .availableDays(new HashSet<>())
                .build();
        anotherUser = userRepository.save(anotherUser);

        // Set admin user as group admin
        testGroup.setAdmin(adminUser);
        
        // Add both regular user and another user to the group
        testGroup.getMembers().add(regularUser);
        testGroup.getMembers().add(anotherUser);
        regularUser.getGroups().add(testGroup);
        anotherUser.getGroups().add(testGroup);
        groupRepository.save(testGroup);
        userRepository.save(regularUser);
        userRepository.save(anotherUser);

        // Try to remove another user using regular user's token
        mockMvc.perform(MockMvcRequestBuilders.delete("/groups/" + testGroup.getId() + "/members/" + anotherUser.getUsername())
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void leaveGroup_shouldSucceed_whenMember() throws Exception {
        // First add the regular user to the group
        testGroup.getMembers().add(regularUser);
        regularUser.getGroups().add(testGroup);
        groupRepository.save(testGroup);
        userRepository.save(regularUser);

        mockMvc.perform(MockMvcRequestBuilders.delete("/groups/" + testGroup.getId() + "/members")
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isNoContent());

        // Verify user was removed
        var updatedGroup = groupRepository.findById(testGroup.getId()).orElseThrow();
        assertFalse(updatedGroup.getMembers().contains(regularUser));
    }

    @Test
    void leaveGroup_shouldFail_whenAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/groups/" + testGroup.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeUserFromGroup_ShouldSucceed_WhenAdminRemovesMember() throws Exception {
        // Create a member user to be removed
        User memberUser = User.builder()
                .username("member@test.com")
                .firstName("Member")
                .lastName("User")
                .password(passwordEncoder.encode("password"))
                .roles(new ArrayList<>(List.of(Role.USER)))
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .availableDays(new HashSet<>())
                .build();
        memberUser = userRepository.save(memberUser);

        // Set admin user as group admin and add member to group
        testGroup.setAdmin(adminUser);
        testGroup.getMembers().add(memberUser);
        memberUser.getGroups().add(testGroup);
        groupRepository.save(testGroup);
        userRepository.save(memberUser);

        // Try to remove member user using admin's token
        mockMvc.perform(MockMvcRequestBuilders.delete("/groups/" + testGroup.getId() + "/members/" + memberUser.getUsername())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isAccepted());

        // Verify member was removed
        var updatedGroup = groupRepository.findById(testGroup.getId()).orElseThrow();
        assertFalse(updatedGroup.getMembers().contains(memberUser));
    }
}