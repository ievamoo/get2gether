package get2gether.controller;

import get2gether.dto.MessageDto;
import get2gether.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    @MessageMapping("/group/{groupId}/chat")
    public void sendMessage(
            @DestinationVariable Long groupId,
            @Payload MessageDto messageDto,
            Message<?> message) {
        var accessor = SimpMessageHeaderAccessor.wrap(message);
        var principal = accessor.getUser();

        if (principal == null) {
            throw new IllegalStateException("Unauthenticated WebSocket user");
        }

        String username = principal.getName();
        messageService.save(groupId, messageDto, username);
    }


}
