package get2gether.controller;

import get2gether.dto.EventDto;
import get2gether.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventDto> createEvent(Authentication authentication, @RequestBody final EventDto eventDto ) {
        var username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(eventDto, username));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventDto> editEvent(Authentication authentication, @RequestBody final EventDto eventDto, @PathVariable final Long eventId) {
        var username = authentication.getName();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(eventService.updateEvent(eventDto, username, eventId));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(Authentication authentication, @PathVariable final Long eventId) {
        var username = authentication.getName();
        eventService.deleteEvent(eventId, username);
        return ResponseEntity.noContent().build();
    }




}
