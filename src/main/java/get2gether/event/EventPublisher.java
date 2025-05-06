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







}
