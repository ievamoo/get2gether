package get2gether.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishInviteStatusChangedEvent(InviteStatusChangedEvent event) {
        log.info("[EventPublisher]: InviteStatusChangedEvent fired for Invite {}", event.getUpdatedInvite().getId());
        publisher.publishEvent(event);
    }

    public void publishEventCreatedEvent(EventCreatedEvent event) {
        log.info("[EventPublisher]: EventCreatedEvent fired for event {}", event.getCreatedEvent().getId());
        publisher.publishEvent(event);
    }

    public void publishGroupCreatedEvent(GroupCreatedEvent event) {
        log.info("[EventPublisher]: GroupCreatedEvent fired for group {}", event.getGroup().getId());
        publisher.publishEvent(event);
    }

    public void publishGroupDeletedEvent(GroupDeletedEvent event) {
        log.info("[EventPublisher]: GroupDeletedEvent fired for group {}", event.getDeletedGroup().getId());
        publisher.publishEvent(event);
    }

    public void publishEventDeletedEvent(EventDeletedEvent event) {
        log.info("[EventPublisher]: EventDeletedEvent fired for event {}", event.getDeletedEvent().getId());
        publisher.publishEvent(event);
    }

    public void publishGroupLeaveEvent(GroupLeaveEvent event) {
        log.info("[EventPublisher]: GroupLeaveEvent fired for group {}", event.getLeftGroup().getId());
        publisher.publishEvent(event);
    }

    public void publishEventAttendanceChangedEvent(EventAttendanceChangedEvent event) {
        log.info("[EventPublisher]: EventAttendanceChangedEvent fired for user {}", event.getUser().getId());
        publisher.publishEvent(event);
    }







}
