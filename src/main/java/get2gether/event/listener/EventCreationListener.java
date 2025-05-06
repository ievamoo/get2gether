package get2gether.event.listener;

import get2gether.event.EventCreatedEvent;
import get2gether.model.Invite;
import get2gether.model.InviteStatus;
import get2gether.model.Type;
import get2gether.model.User;
import get2gether.repository.UserRepository;
import get2gether.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventCreationListener {

    private final UserRepository userRepository;

    @EventListener
    public void handleEventCreation(EventCreatedEvent event) {
        var members = event.getCreatedEvent().getGroup().getMembers();
        var createdEvent = event.getCreatedEvent();
        members.forEach(user -> user.getInvitesReceived()
                .add(createInvite(createdEvent.getId(), createdEvent.getHostUsername(), user, createdEvent.getName())));
        userRepository.saveAll(members);
        log.info("[EventCreationListener]: event id {} added to all group members invite list", createdEvent.getId());
    }

    private Invite createInvite (Long eventId, String senderUsername, User receiver, String typeName) {
        return Invite.builder()
                .type(Type.EVENT)
                .typeId(eventId)
                .typeName(typeName)
                .senderUsername(senderUsername)
                .receiver(receiver)
                .status(InviteStatus.PENDING)
                .build();
    }

}
