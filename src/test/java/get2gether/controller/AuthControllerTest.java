package get2gether.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import get2gether.TestData;
import get2gether.dto.AuthRequestDto;
import get2gether.enums.Role;
import get2gether.model.User;
import get2gether.repository.UserRepository;
import get2gether.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void registerUser() throws Exception {
        var registerDto = TestData.getRegisterRequestDto();
//        var registerDto = RegisterRequestDto.builder()
//                .username("newUser@mail.com")
//                .firstName("NewUserName")
//                .lastName("NewUserLastName")
//                .password("newPassword")
//                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt").exists());
    }

    @Test
    void createAuthenticationToken() throws Exception {

        User testUser = User.builder()
                .id(1L)
                .username("testuser@example.com")
                .firstName("TestName")
                .lastName("TestLastName")
                .password(passwordEncoder.encode("password"))
                .roles(List.of(Role.USER))
                .build();

        userRepository.save(testUser);

        var loginDto = AuthRequestDto.builder()
                .username("testuser@example.com")
                .password("password")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt").exists());

    }
}