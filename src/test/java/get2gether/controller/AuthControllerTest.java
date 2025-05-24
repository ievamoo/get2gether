package get2gether.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import get2gether.dto.AuthRequestDto;
import get2gether.dto.RegisterRequestDto;
import get2gether.enums.Role;
import get2gether.model.User;
import get2gether.repository.UserRepository;
import get2gether.security.JwtUtil;
import get2gether.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Create a new test user
        User testUser = User.builder()
                .username("test@gmail.com")
                .firstName("Test")
                .lastName("User")
                .password(passwordEncoder.encode("password123"))
                .roles(List.of(Role.USER))
                .availableDays(new HashSet<>())
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .build();
        
        userRepository.save(testUser);
    }

    @Test
    void register_shouldReturnCreated_whenValidInput() throws Exception {
        var registerRequest = new RegisterRequestDto(
                "newuser@gmail.com",
                "password123",
                "New",
                "User"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwt").exists());
    }

    @Test
    void register_shouldReturnBadRequest_whenUsernameExists() throws Exception {
        var registerRequest = new RegisterRequestDto(
                "test@gmail.com", // Using existing username
                "password123",
                "New",
                "User"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnOk_whenValidCredentials() throws Exception {
        var loginRequest = new AuthRequestDto(
                "test@gmail.com",
                "password123"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists());
    }

    @Test
    void login_shouldReturnUnauthorized_whenInvalidCredentials() throws Exception {
        var loginRequest = new AuthRequestDto(
                "test@gmail.com",
                "wrongpassword"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturnUnauthorized_whenUserNotFound() throws Exception {
        var loginRequest = new AuthRequestDto(
                "nonexistent@gmail.com",
                "password123"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}