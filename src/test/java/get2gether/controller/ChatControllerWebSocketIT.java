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
                    MessageDto messageDto = MessageDto.builder()
                            .message("Hello Group!")
                            .senderUsername(testUser.getUsername())
                            .build();
                    session.send("/app/group/" + groupId + "/chat", messageDto);
                    messageSentFuture.complete(true);
                } catch (Exception e) {
                    messageSentFuture.completeExceptionally(e);
                }
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                messageSentFuture.completeExceptionally(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                messageSentFuture.completeExceptionally(exception);
            }
        };

        stompClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, sessionHandler);

        assertThat(messageSentFuture.get(5, TimeUnit.SECONDS)).isTrue();

        Thread.sleep(1000);

        List<Message> messages = messageRepository.findByGroupId(groupId);
        assertThat(messages).isNotEmpty();

        Message savedMessage = messages.get(0);
        assertThat(savedMessage.getMessage()).isEqualTo("Hello Group!");
        assertThat(savedMessage.getSenderUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    void shouldNotSendGroupMessage_whenUserIsUnauthenticated() throws Exception {
        String url = "http://localhost:" + port + "/ws";

        StompHeaders connectHeaders = new StompHeaders();

        CompletableFuture<Boolean> messageSentFuture = new CompletableFuture<>();
        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                try {
                    MessageDto messageDto = MessageDto.builder()
                            .message("Hello Group!")
                            .senderUsername(testUser.getUsername())
                            .build();
                    session.send("/app/group/" + groupId + "/chat", messageDto);
                    messageSentFuture.complete(true);
                } catch (Exception e) {
                    messageSentFuture.completeExceptionally(e);
                }
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                messageSentFuture.completeExceptionally(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                messageSentFuture.completeExceptionally(exception);
            }
        };

        stompClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, sessionHandler);

        assertThatThrownBy(() -> messageSentFuture.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(ConnectionLostException.class);

        List<Message> messages = messageRepository.findByGroupId(groupId);
        assertThat(messages).isEmpty();
    }
}