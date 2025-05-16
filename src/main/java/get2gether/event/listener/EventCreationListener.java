package get2gether.event.listener;

import get2gether.event.EventCreatedEvent;
import get2gether.manualMapper.ManualInviteMapper;
import get2gether.model.Invite;
import get2gether.model.Type;
import get2gether.model.User;
import get2gether.repository.InviteRepository;
import get2gether.repository.UserRepository;
import get2gether.service.InviteNotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventCreationListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ManualInviteMapper manualInviteMapper;
    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;


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
//                    log.info("[EventCreationListener] : dto for username {}", invite.getReceiver());
                    var savedInvite = inviteRepository.save(invite);
                    log.info("[EventCreationListener] Invite saved for user: {}", user.getUsername());

                    var inviteDto = manualInviteMapper.modelToDto(savedInvite);
                    messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/invites", inviteDto);

//                    inviteNotifierService.sendInviteToUser(inviteDto);
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

//    @EventListener
//    public void updateHostAvailability(EventCreatedEvent event) {
//        var host = userRepository.findByUsername(event.getCreatedEvent().getHostUsername());
//        if (host.isEmpty()) return;
//        host.get().getAvailableDays().remove(event.getCreatedEvent().getDate());
//        userRepository.save(host.get());
//        log.info("[EventCreationListener] Available days updated. Date removed: {}", event.getCreatedEvent().getDate());
//    }
}
