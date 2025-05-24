package get2gether.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import get2gether.dto.InviteDto;
import get2gether.enums.Type;
import get2gether.service.InviteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InviteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        inviteService = Mockito.mock(InviteService.class);
        InviteController controller = new InviteController(inviteService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void createGroupInvite_shouldReturnCreated() throws Exception {
        InviteDto dto = InviteDto.builder()
                .type(Type.GROUP)
                .typeId(123L)
                .typeName("Group A")
                .groupName("Group A")
                .eventDate(LocalDate.now())
                .senderUsername("testuser")
                .receiverUsernames(Set.of("user1", "user2"))
                .build();

        when(inviteService.createNewInviteWhenGroupAlreadyExists(any(), eq("testuser")))
                .thenReturn("Invite created successfully");

        mockMvc.perform(post("/invites")
                        .principal(new TestingAuthenticationToken("testuser", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Invite created successfully"))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));
    }

    @Test
    void respondToInvite_shouldReturnOk() throws Exception {
        InviteDto dto = InviteDto.builder().accepted(true).build();

        mockMvc.perform(patch("/invites/42")
                        .principal(new TestingAuthenticationToken("testuser", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(inviteService).handleInviteResponse("testuser", 42L, true);
    }
}
