package get2gether.event;

import get2gether.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

/**
 * Event that is published when a user's attendance status for an event changes.
 * Contains information about the event date, user, and their new attendance status.
 */
@Getter
public class EventAttendanceChangedEvent extends ApplicationEvent {

    private final LocalDate eventDate;
    private final User user;
    private final Boolean isGoing;

    public EventAttendanceChangedEvent(Object source, LocalDate eventDate, User user, Boolean isGoing) {
        super(source);
        this.eventDate = eventDate;
        this.user = user;
        this.isGoing = isGoing;
    }
}
