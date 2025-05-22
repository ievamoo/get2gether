package get2gether.event.listener;

import get2gether.event.EventAttendanceChangedEvent;
import get2gether.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventAttendanceChangedListener {

    private final UserRepository userRepository;

    /**
     * Updates the host's availability when they mark themselves as attending an event.
     * If the user is going to the event, removes the event date from their available days
     * to prevent scheduling conflicts.
     *
     * @param event the event containing information about the attendance change
     */
    @EventListener
    public void updateHostAvailability(EventAttendanceChangedEvent event) {
        var user = event.getUser();
        if (event.getIsGoing()) {
            user.getAvailableDays().remove(event.getEventDate());
            userRepository.save(user);
            log.info("[EventAttendanceChangedListener] Available days updated. Date removed: {}", event.getEventDate());
        }
    }
}
