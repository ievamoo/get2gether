package get2gether.controller;

import get2gether.dto.EventDto;
import get2gether.dto.EventStatusDto;
import get2gether.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventDto> createEvent(Authentication authentication, @RequestBody final EventDto eventDto ) {
        var username = authentication.getName();
        var createdEvent = eventService.createEvent(eventDto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventDto> editEvent(
            Authentication authentication,
            @RequestBody final EventDto eventDto,
            @PathVariable final Long eventId) {
        var username = authentication.getName();
        var updatedEvent = eventService.updateEvent(eventDto, username, eventId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedEvent);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(Authentication authentication, @PathVariable final Long eventId) {
        var username = authentication.getName();
        eventService.deleteEvent(eventId, username);
        return ResponseEntity.noContent().build();
    }

    //TODO patikrinti, ar statusas jau nera toks, koki paduoda

    @PatchMapping("/{eventId}/status")
    public ResponseEntity<List<EventDto>> toggleEventAttendance(
            Authentication authentication,
            @PathVariable final Long eventId,
            @RequestBody final EventStatusDto dto) {
        var username = authentication.getName();
        var updatedUserEvents = eventService.toggleEventAttendance(username, eventId, dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedUserEvents);
    }

}
