package get2gether.event;

import get2gether.enums.EventAction;
import get2gether.model.Event;
import get2gether.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

@Getter
public class EventActionEvent extends ApplicationEvent {
    private final EventAction action;
    private final Event event;
    private final LocalDate eventDate;
    private final User user;
    private final Boolean isGoing;

    // Constructor for CREATED and DELETED actions
    public EventActionEvent(Object source, EventAction action, Event event) {
        super(source);
        if (action != EventAction.CREATED && action != EventAction.DELETED) {
            throw new IllegalArgumentException("This constructor is only for CREATED and DELETED actions");
        }
        this.action = action;
        this.event = event;
        this.eventDate = null;
        this.user = null;
        this.isGoing = null;
    }

    // Constructor for ATTENDANCE_CHANGED action
    public EventActionEvent(Object source, Event event, LocalDate eventDate, User user, Boolean isGoing) {
        super(source);
        this.action = EventAction.ATTENDANCE_CHANGED;
        this.event = event;
        this.eventDate = eventDate;
        this.user = user;
        this.isGoing = isGoing;
    }
} 