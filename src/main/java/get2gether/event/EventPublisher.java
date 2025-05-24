package get2gether.event;

import get2gether.enums.EventAction;
import get2gether.enums.GroupAction;
import get2gether.model.Event;
import get2gether.model.Group;
import get2gether.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

/**
 * Central event publisher for the Get2Gather application.
 * Handles the publishing of various application events such as invites, events, and groups.
 * Uses Spring's ApplicationEventPublisher to broadcast events to registered listeners.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishInviteStatusChangedEvent(InviteStatusChangedEvent event) {
        log.info("[EventPublisher]: InviteStatusChangedEvent fired for Invite {}", event.getUpdatedInvite().getId());
        publisher.publishEvent(event);
    }

    public void publishEventAction(EventAction action, Event event) {
        EventActionEvent eventActionEvent = new EventActionEvent(this, action, event);
        log.info("[EventPublisher]: EventActionEvent fired for event {} with action {}", 
            event.getId(), action);
        publisher.publishEvent(eventActionEvent);
    }

    public void publishEventAttendanceChanged(Event event, LocalDate eventDate, User user, Boolean isGoing) {
        EventActionEvent eventActionEvent = new EventActionEvent(this, event, eventDate, user, isGoing);
        log.info("[EventPublisher]: EventActionEvent fired for attendance change - user {}, event {}", 
            user.getId(), event.getId());
        publisher.publishEvent(eventActionEvent);
    }


    public void publishGroupAction(GroupAction action, Group group) {
        GroupActionEvent event = new GroupActionEvent(this, group, action);
        log.info("[EventPublisher]: GroupActionEvent fired for group {} with action {}", 
            group.getId(), action);
        publisher.publishEvent(event);
    }

    public void publishGroupAction(GroupAction action, Group group, User user) {
        GroupActionEvent event = new GroupActionEvent(this, group, action, user);
        log.info("[EventPublisher]: GroupActionEvent fired for group {} with action {} and user {}", 
            group.getId(), action, user.getId());
        publisher.publishEvent(event);
    }

    public void publishGroupAction(GroupAction action, Group group, Set<String> invitedUsernames) {
        GroupActionEvent event = new GroupActionEvent(this, group, action, invitedUsernames);
        log.info("[EventPublisher]: GroupActionEvent fired for group {} with action {} and {} invited users", 
            group.getId(), action, invitedUsernames.size());
        publisher.publishEvent(event);
    }

}
