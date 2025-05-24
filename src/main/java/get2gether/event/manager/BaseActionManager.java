package get2gether.event.manager;

import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseActionManager {
    private final SimpMessagingTemplate messagingTemplate;
    protected final InviteService inviteService;

    protected void notifyUser(String username, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(username, destination, message);
    }

    protected void notifyGroup(Long groupId, String message) {
        messagingTemplate.convertAndSend("/topic/group/" + groupId, message);
    }
}

