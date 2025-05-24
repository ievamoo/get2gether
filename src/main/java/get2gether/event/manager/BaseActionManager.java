package get2gether.event.manager;

import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RequiredArgsConstructor
public abstract class BaseActionManager {
    protected final SimpMessagingTemplate messagingTemplate;
    protected final InviteService inviteService;
    protected final Logger log;

    protected BaseActionManager(SimpMessagingTemplate messagingTemplate, InviteService inviteService) {
        this.messagingTemplate = messagingTemplate;
        this.inviteService = inviteService;
        this.log = LoggerFactory.getLogger(getClass());
    }

    protected void notifyUser(String username, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(username, destination, message);
    }

    protected void notifyGroup(Long groupId, String message) {
        messagingTemplate.convertAndSend("/topic/group/" + groupId, message);
    }
} 