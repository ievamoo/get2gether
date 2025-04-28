package get2gether.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import get2gether.dto.UserDto;
import get2gether.model.Role;
import get2gether.model.User;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L)
                .username("testuser@example.com")
                .firstName("TestName")
                .lastName("TestLastName")
                .password(passwordEncoder.encode("password"))
                .roles(List.of(Role.USER))
                .build();

        userRepository.save(testUser);

        UserDetails testUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser@example.com")
                .password("irrelevant_in_token")
                .authorities("USER")
                .build();

         token = jwtUtil.generateToken(testUserDetails);
    }


    @Test
    void getCurrentUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("TestName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("TestLastName"));
    }

    @Test
    void updateCurrentUser() throws Exception {
        var updateDto = UserDto.builder()
                .firstName("UpdatedName")
                .lastName("UpdatedLastName")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isAccepted())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("UpdatedName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("UpdatedLastName"));
    }

    @Test
    void deleteCurrentUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void getAllUsers() throws Exception {

        var admin = User.builder()
                .username("testAdmin@gmail.com")
                .firstName("TestAdminName")
                .lastName("TestAdminLastName")
                .password(passwordEncoder.encode("testAdmin123"))
                .roles(List.of(Role.ADMIN))
                .build();

        userRepository.save(admin);

        UserDetails adminUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("testAdmin@gmail.com")
                .password("irrelevant_in_token")
                .authorities("ADMIN")
                .build();

        var token = jwtUtil.generateToken(adminUserDetails);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].username").value("testuser@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].firstName").value("TestName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].lastName").value("TestLastName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].username").value("testAdmin@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].firstName").value("TestAdminName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].lastName").value("TestAdminLastName"));
    }
}