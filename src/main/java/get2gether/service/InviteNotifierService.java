package get2gether.service;

import get2gether.dto.InviteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteNotifierService {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    private final InviteService inviteService;

    public void sendInviteToUser(InviteDto invite) {
        var usernames = invite.getReceiverUsernames();

        if (usernames == null || usernames.isEmpty()) {
            log.warn("Invite {} has no receiver usernames", invite.getId());
            return;
        }
        String username = usernames.iterator().next();
        if (simpUserRegistry.getUser(username) != null) {
            messagingTemplate.convertAndSend("/topic", "Hello, World");
            log.info("Sent invite {} to {}", invite.getId(), username);
        } else {
            log.warn("User {} is not connected. Invite not sent via WebSocket.", username);
        }
    }
}
