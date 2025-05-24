package get2gether.controller;

import get2gether.dto.MessageDto;
import get2gether.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * Controller responsible for handling real-time group chat functionality through WebSocket.
 * Manages WebSocket message handling for group chats, including:
 * - Real-time message delivery to group members
 * - Message persistence and validation
 * - Authentication and authorization of chat participants
 * - WebSocket connection management
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final MessageService messageService;

    /**
     * Handles incoming WebSocket messages for group chat functionality.
     * This method processes messages sent to a specific group chat and saves them using the message service.
     * It requires the user to be authenticated to process the message.
     *
     * @param groupId    The unique identifier of the group where the message is being sent
     * @param messageDto The message data transfer object containing the message content and metadata
     * @param message    The raw WebSocket message containing additional information like headers
     * @throws IllegalStateException if the user is not authenticated
     */
    @MessageMapping("/group/{groupId}/chat")
    public void sendGroupMessage(@DestinationVariable Long groupId,
                                 @Payload MessageDto messageDto,
                                 Message<?> message) {
        var accessor = SimpMessageHeaderAccessor.wrap(message);
        var principal = accessor.getUser();

        if (principal == null) {
            log.warn("Unauthenticated WebSocket user tried to send a message to group {}", groupId);
            throw new IllegalStateException("Unauthenticated WebSocket user");
        }

        String username = principal.getName();
        log.info("Received message from '{}' for groupId: {}", username, groupId);

        messageService.save(groupId, messageDto, username);
    }

}
