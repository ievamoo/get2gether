package get2gether.event;

import get2gether.model.Event;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when an event is deleted.
 * Contains information about the deleted event.
 */
@Getter
public class EventDeletedEvent extends ApplicationEvent {

    private final Event deletedEvent;

    public EventDeletedEvent(Object source, Event deletedEvent) {
        super(source);
        this.deletedEvent = deletedEvent;
    }
}
