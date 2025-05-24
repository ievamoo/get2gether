package get2gether.event.manager;

import get2gether.event.InviteStatusChangedEvent;
import get2gether.model.Invite;
import get2gether.model.User;
import get2gether.service.EventService;
import get2gether.service.GroupService;
import get2gether.service.InviteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class InviteActionManager extends BaseActionManager {

    private final GroupService groupService;
    private final EventService eventService;

    public InviteActionManager(SimpMessagingTemplate messagingTemplate,
                               InviteService inviteService,
                               GroupService groupService,
                               EventService eventService) {
        super(messagingTemplate, inviteService);
        this.groupService = groupService;
        this.eventService = eventService;
    }

    @EventListener
    @Transactional
    public void handleInviteResponse(InviteStatusChangedEvent event) {
        var invite = event.getUpdatedInvite();
        var receiver = invite.getReceiver();
        var accepted = event.getAccepted();

        log.info("[InviteActionManager]: handling {} invite response..", invite.getType());

        switch (invite.getType()) {
            case GROUP -> handleGroupInviteResponse(accepted, receiver, invite);
            case EVENT -> handleEventInviteResponse(accepted, invite, receiver);
            default -> log.warn("[InviteActionManager]: Unknown invite type: {}", invite.getType());
        }
    }

    private void handleGroupInviteResponse(boolean accepted, User receiver, Invite invite) {
        if (!accepted) {
            log.info("[InviteActionManager]: Group invite rejected by user {}", receiver.getUsername());
            return;
        }

        groupService.addMember(invite.getTypeId(), receiver);
        log.info("[InviteActionManager]: User {} added to group {}", receiver.getUsername(), invite.getTypeId());
        notifyGroup(invite.getTypeId(), String.format("User %s joined the group", receiver.getUsername()));
    }

    private void handleEventInviteResponse(boolean accepted, Invite invite, User receiver) {
        if (!accepted) {
            eventService.removeUserFromEvent(invite.getTypeId(), receiver);
            log.info("[InviteActionManager]: User {} declined event {}", receiver.getUsername(), invite.getTypeId());
            return;
        }

        eventService.addUserToEvent(invite.getTypeId(), receiver);
        log.info("[InviteActionManager]: User {} marked as going to event {}", receiver.getUsername(), invite.getTypeId());
    }
}
