package get2gether.event.manager;

import get2gether.enums.Type;
import get2gether.event.EventActionEvent;
import get2gether.repository.UserRepository;
import get2gether.service.InviteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class EventActionManager extends BaseActionManager {
    private final UserRepository userRepository;

    public EventActionManager(SimpMessagingTemplate messagingTemplate,
                              InviteService inviteService,
                              UserRepository userRepository) {
        super(messagingTemplate, inviteService);
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void handleEventAction(EventActionEvent event) {
        log.info("[EventActionManager] Handling event action: {} for event: {}",
                event.getAction(), event.getEvent().getName());

        switch (event.getAction()) {
            case CREATED -> handleEventCreation(event);
            case DELETED -> handleEventDeletion(event);
            case ATTENDANCE_CHANGED -> handleAttendanceChange(event);
        }
    }

    private void handleEventCreation(EventActionEvent event) {
        var members = event.getEvent().getGroup().getMembers();
        var createdEvent = event.getEvent();

        log.info("[EventActionManager] Processing {} members for event", members.size());
        members.stream()
                .filter(user -> !user.getUsername().equalsIgnoreCase(createdEvent.getHostUsername()))
                .forEach(user -> {
                    var inviteDto = inviteService.createEventInvite(createdEvent, user);
                    notifyUser(user.getUsername(), "/queue/invites", inviteDto);
                });
    }

    private void handleEventDeletion(EventActionEvent event) {
        var eventInvites = inviteService.getInvitesByTypeAndTypeId(Type.EVENT, event.getEvent().getId());
        inviteService.deleteInvite(eventInvites);
        log.info("[EventActionManager]: invites deleted for event id {}", event.getEvent().getId());

        event.getEvent().getGroup().getMembers().forEach(member ->
                notifyUser(member.getUsername(), "/queue/event-deleted", String.valueOf(event.getEvent().getGroup().getId()))
        );
    }

    private void handleAttendanceChange(EventActionEvent event) {
        if (event.getIsGoing()) {
            var user = event.getUser();
            user.getAvailableDays().remove(event.getEventDate());
            userRepository.save(user); // still allowed here
            log.info("[EventActionManager] Available days updated. Date removed: {}", event.getEventDate());
        }
    }
}


