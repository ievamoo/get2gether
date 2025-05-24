package get2gether.event;

import get2gether.model.Event;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when a new event is created.
 * Contains information about the newly created event.
 */
@Getter
public class EventCreatedEvent extends ApplicationEvent {

    private final Event createdEvent;

    public EventCreatedEvent(Object source, Event createdEvent) {
        super(source);
        this.createdEvent = createdEvent;
    }

}
