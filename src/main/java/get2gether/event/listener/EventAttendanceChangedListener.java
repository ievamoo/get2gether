package get2gether.event.listener;

import get2gether.event.EventAttendanceChangedEvent;
import get2gether.event.EventCreatedEvent;
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
