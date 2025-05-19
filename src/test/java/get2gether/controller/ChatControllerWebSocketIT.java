package get2gether.controller;

import get2gether.dto.MessageDto;
import get2gether.model.Group;
import get2gether.model.Message;
import get2gether.model.Role;
import get2gether.model.User;
import get2gether.repository.GroupRepository;
import get2gether.repository.MessageRepository;
import get2gether.repository.UserRepository;
import get2gether.security.JwtUtil;
import get2gether.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

//@ActiveProfiles("test")
@Slf4j
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class ChatControllerWebSocketIT {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MessageRepository messageRepository;

    private String token;
    private Long groupId;
    private User testUser;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        // Create unique test user
        testUser = User.builder()
                .username("test" + UUID.randomUUID() + "@gmail.com")
                .firstName("TestName")
                .lastName("TestLastName")
                .password("encoded_password")
                .roles(List.of(Role.USER))
                .availableDays(new HashSet<>())
                .groups(new HashSet<>())
                .build();
        userRepository.save(testUser);

        // Create unique test group
        testGroup = Group.builder()
                .name("Test Group " + UUID.randomUUID())
                .members(Set.of(testUser))
                .admin(testUser)
                .build();
        groupRepository.save(testGroup);
        groupId = testGroup.getId();

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(testUser.getUsername());
        token = jwtUtil.generateToken(userDetails);

        List<Transport> transports = List.of(
                new WebSocketTransport(new StandardWebSocketClient()),
                new RestTemplateXhrTransport()
        );

        WebSocketClient transportClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(transportClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void shouldSendGroupMessage_whenUserIsAuthenticated() throws Exception {
        String url = "http://localhost:" + port + "/ws";

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        CompletableFuture<Boolean> messageSentFuture = new CompletableFuture<>();
        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                try {
                    log.info("WebSocket connected successfully");
                    MessageDto messageDto = MessageDto.builder()
                            .message("Hello Group!")
                            .senderUsername(testUser.getUsername())
                            .build();
                    log.info("Sending message to group {}: {}", groupId, messageDto);
                    session.send("/app/group/" + groupId + "/chat", messageDto);
                    messageSentFuture.complete(true);
                } catch (Exception e) {
                    log.error("Error sending message", e);
                    messageSentFuture.completeExceptionally(e);
                }
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                log.error("STOMP error", exception);
                messageSentFuture.completeExceptionally(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                log.error("Transport error", exception);
                messageSentFuture.completeExceptionally(exception);
            }
        };

        stompClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, sessionHandler);

        assertThat(messageSentFuture.get(5, TimeUnit.SECONDS)).isTrue();

        // Add a small delay to allow message processing
        Thread.sleep(1000);

        List<Message> messages = messageRepository.findByGroupId(groupId);
        log.info("Found {} messages in database for group {}", messages.size(), groupId);
        assertThat(messages).isNotEmpty();

        Message savedMessage = messages.get(0);
        // Log only the non-lazy fields to avoid LazyInitializationException
        log.info("Saved message - id: {}, message: {}, sender: {}",
                savedMessage.getId(),
                savedMessage.getMessage(),
                savedMessage.getSenderUsername());

        assertThat(savedMessage.getMessage()).isEqualTo("Hello Group!");
        assertThat(savedMessage.getSenderUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    void shouldNotSendGroupMessage_whenUserIsUnauthenticated() throws Exception {
        String url = "http://localhost:" + port + "/ws";

        // No Authorization header → unauthenticated
        StompHeaders connectHeaders = new StompHeaders();

        CompletableFuture<Boolean> messageSentFuture = new CompletableFuture<>();
        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                try {
                    log.info("WebSocket connected successfully (unauthenticated)");
                    MessageDto messageDto = MessageDto.builder()
                            .message("Hello Group!")
                            .senderUsername(testUser.getUsername())
                            .build();
                    log.info("Attempting to send message to group {}: {}", groupId, messageDto);
                    session.send("/app/group/" + groupId + "/chat", messageDto);
                    messageSentFuture.complete(true);
                } catch (Exception e) {
                    log.error("Error sending message", e);
                    messageSentFuture.completeExceptionally(e);
                }
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                log.error("STOMP error", exception);
                messageSentFuture.completeExceptionally(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                log.error("Transport error", exception);
                messageSentFuture.completeExceptionally(exception);
            }
        };

        stompClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, sessionHandler);

        assertThatThrownBy(() -> messageSentFuture.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(ConnectionLostException.class);

        List<Message> messages = messageRepository.findByGroupId(groupId);
        log.info("Found {} messages in database for group {} (should be 0)", messages.size(), groupId);
        assertThat(messages).isEmpty();
    }

}