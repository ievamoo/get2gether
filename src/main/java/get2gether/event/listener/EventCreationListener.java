package get2gether.event.listener;

import get2gether.event.EventCreatedEvent;
import get2gether.mapper.InviteMapper;
import get2gether.model.Invite;
import get2gether.model.Type;
import get2gether.model.User;
import get2gether.repository.InviteRepository;
import get2gether.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventCreationListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final InviteMapper inviteMapper;
    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;

    /**
     * Handles the event creation event by creating and sending invites to group members.
     * For each group member (except the host), it:
     * 1. Creates an event invite
     * 2. Saves the invite to the database
     * 3. Sends a WebSocket notification to the user
     *
     * @param event the event containing information about the created event
     */
    @EventListener
    public void handleEventCreation(EventCreatedEvent event) {
        log.info("[EventCreationListener] Event creation triggered for event: {}", event.getCreatedEvent().getName());

        var members = event.getCreatedEvent().getGroup().getMembers();
        var createdEvent = event.getCreatedEvent();

        log.info("[EventCreationListener] Processing {} members for event", members.size());
        members.stream()
                .filter(user -> !user.getUsername().equalsIgnoreCase(createdEvent.getHostUsername()))
                .forEach(user -> {
                    var invite = createInvite(
                            createdEvent.getId(),
                            createdEvent.getHostUsername(),
                            user,
                            createdEvent.getName()
                    );
                    user.getInvitesReceived().add(invite);
                    invite.setReceiver(user);
                    var savedInvite = inviteRepository.save(invite);
                    log.info("[EventCreationListener] Invite saved for user: {}", user.getUsername());

                    var inviteDto = inviteMapper.modelToDto(savedInvite);
                    messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/invites", inviteDto);
                });
    }

    private Invite createInvite(Long eventId, String senderUsername, User receiver, String typeName) {
        return Invite.builder()
                .type(Type.EVENT)
                .typeId(eventId)
                .typeName(typeName)
                .senderUsername(senderUsername)
                .receiver(receiver)
                .build();
    }

}
