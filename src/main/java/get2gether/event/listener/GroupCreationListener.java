package get2gether.event.listener;

import get2gether.event.GroupCreatedEvent;
import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupCreationListener {

    private final InviteService inviteService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void sendInvitesToSelectedUsers(GroupCreatedEvent event) {
        if (event.getInvitedUsernames().isEmpty()) {
            log.info("No users were invited on group {} creation", event.getGroup().getName());
            return;
        }

        var invitesToSend = inviteService.createInvitesOnGroupCreation(event.getGroup(), event.getInvitedUsernames());
        invitesToSend.forEach(inviteDto ->{
            var receiverUsername = inviteDto.getReceiverUsernames().iterator().next();
            messagingTemplate.convertAndSendToUser(receiverUsername, "/queue/invites", inviteDto);
        } );
    }
}
