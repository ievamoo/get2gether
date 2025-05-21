package get2gether.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import get2gether.dto.GroupDto;
import get2gether.model.Group;
import get2gether.model.Role;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
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

    @Autowired
    private EntityManager entityManager;

    private String token;
    private User testUser;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser@example.com")
                .firstName("TestName")
                .lastName("TestLastName")
                .password(passwordEncoder.encode("password"))
                .roles(List.of(Role.USER))
                .availableDays(new HashSet<>())  // Initialize availableDays as empty set
                .build();

        testUser = userRepository.save(testUser);

        UserDetails testUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser@example.com")
                .password("irrelevant_in_token")
                .authorities("USER")
                .build();

        token = jwtUtil.generateToken(testUserDetails);

        testGroup = Group.builder()
                .name("Test Group")
                .admin(testUser)
                .members(new HashSet<>(Set.of(testUser)))
                .events(new ArrayList<>())
                .messages(new ArrayList<>())
                .groupColor("#FF0000")
                .build();

        testGroup = groupRepository.save(testGroup);
    }

    @Test
    void getGroupById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/groups/" + testGroup.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Group"))
                .andExpect(jsonPath("$.admin.username").value("testuser@example.com"));
    }

    @Test
    void getAllGroupEvents() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/groups/" + testGroup.getId() + "/events")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createGroup() throws Exception {
        var groupDto = GroupDto.builder()
                .name("New Test Group")
                .invitedUsernames(new HashSet<>())  // Initialize empty set of invited users
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/groups")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Test Group"))
                .andExpect(jsonPath("$.admin.username").value("testuser@example.com"));
    }

    @Test
    void editGroupName() throws Exception {
        var editedGroupDto = GroupDto.builder()
                .name("Updated Group Name")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/groups/" + testGroup.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editedGroupDto)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.name").value("Updated Group Name"));
    }

    @Test
    void deleteGroup() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/groups/" + testGroup.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeUserFromGroup() throws Exception {
        var memberToRemove = User.builder()
                .username("member@example.com")
                .firstName("Member")
                .lastName("Test")
                .password(passwordEncoder.encode("password"))
                .roles(List.of(Role.USER))
                .build();
        
        memberToRemove = userRepository.save(memberToRemove);
        testGroup.getMembers().add(memberToRemove);
        groupRepository.save(testGroup);

        mockMvc.perform(MockMvcRequestBuilders.delete("/groups/" + testGroup.getId() + "/members/member@example.com")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void leaveGroup() throws Exception {
        // Create a regular member user
        User memberUser = User.builder()
                .username("member@example.com")
                .firstName("Member")
                .lastName("User")
                .password(passwordEncoder.encode("password"))
                .roles(List.of(Role.USER))
                .availableDays(new HashSet<>())
                .build();
        memberUser = userRepository.save(memberUser);

        // Add member to group
        testGroup.getMembers().add(memberUser);
        groupRepository.save(testGroup);

        // Create authentication token for member
        UserDetails memberUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("member@example.com")
                .password("irrelevant_in_token")
                .authorities("USER")
                .build();
        String memberToken = jwtUtil.generateToken(memberUserDetails);

        // Force refresh from database to ensure proper initialization
        entityManager.flush();
        entityManager.clear();

        // Perform the leave group operation
        mockMvc.perform(MockMvcRequestBuilders.delete("/groups/" + testGroup.getId() + "/members")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isNoContent());
    }
}