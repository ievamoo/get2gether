package get2gether.event;

import get2gether.model.Event;
import get2gether.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class EventCreatedEvent extends ApplicationEvent {

    private final Event createdEvent;

    public EventCreatedEvent(Object source, Event createdEvent) {
        super(source);
        this.createdEvent = createdEvent;
    }

}
