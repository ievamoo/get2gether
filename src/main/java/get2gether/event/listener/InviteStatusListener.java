package get2gether.event.listener;

import get2gether.event.InviteStatusChangedEvent;
import get2gether.model.Invite;
import get2gether.model.User;
import get2gether.repository.InviteRepository;
import get2gether.service.EventService;
import get2gether.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InviteStatusListener {

    private final GroupService groupService;
    private final EventService eventService;
    private final InviteRepository inviteRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @EventListener
    public void handleInviteResponse(InviteStatusChangedEvent event) {
        var invite = event.getUpdatedInvite();
        var receiver = invite.getReceiver();
        var accepted = event.getAccepted();
        System.out.println(accepted);
        log.info("[InviteStatusListener]: handling {} invite response..", invite.getType());
        switch (invite.getType()) {
            case GROUP -> handleGroupInviteResponse(accepted, receiver, invite);
            case EVENT -> handleEventInviteResponse(accepted, invite, receiver);
            default -> log.warn("[InviteStatusListener]: Unknown invite type: {}", invite.getType());
        }
    }

    private void handleEventInviteResponse(Boolean accepted, Invite invite, User receiver) {
        if (!accepted) {
            eventService.removeUserFromEvent(invite.getTypeId(), receiver);
            log.info("[InviteStatusListener]: User {} declined event {}", receiver.getUsername(), invite.getTypeId());
            return;
        }
        eventService.addUserToEvent(invite.getTypeId(), receiver);
        log.info("[InviteStatusListener]: User {} marked as going to event {}", receiver.getUsername(), invite.getTypeId());
    }

    private void handleGroupInviteResponse(Boolean accepted, User receiver, Invite invite) {
        if (!accepted) {
            log.info("[InviteStatusListener]: Group invite rejected by user {}", receiver.getUsername());
            return;
        }

        groupService.addMember(invite.getTypeId(), receiver);
        log.info("[InviteStatusListener]: User {} added to group {}", receiver.getUsername(), invite.getTypeId());
        var message = String.format("User %s joined the group", receiver.getUsername());
        messagingTemplate.convertAndSend("/topic/group/" + invite.getTypeId(), message);
    }
}
