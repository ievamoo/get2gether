package get2gether.event;

import get2gether.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

@Getter
public class EventAttendanceChangedEvent  extends ApplicationEvent {

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
