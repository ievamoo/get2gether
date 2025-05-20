package get2gether.controller;

import get2gether.dto.EventDto;
import get2gether.dto.EventStatusDto;
import get2gether.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    /**
     * Creates a new event.
     * This endpoint allows authenticated users to create events within their groups.
     *
     * @param authentication The authentication object containing the current user's details
     * @param eventDto The event data transfer object containing event details
     * @return ResponseEntity containing the created event details with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<EventDto> createEvent(Authentication authentication, @RequestBody final EventDto eventDto) {
        var username = authentication.getName();
        var createdEvent = eventService.createEvent(eventDto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Updates an existing event.
     * This endpoint allows event hosts to modify event details.
     *
     * @param authentication The authentication object containing the current user's details
     * @param eventDto The updated event data
     * @param eventId The ID of the event to update
     * @return ResponseEntity with HTTP 202 status if update is successful
     */
    @PatchMapping("/{eventId}")
    public ResponseEntity<Void> editEvent(Authentication authentication,
                                          @RequestBody final EventDto eventDto,
                                          @PathVariable final Long eventId) {
        var username = authentication.getName();
        eventService.updateEvent(eventDto, username, eventId);
        return ResponseEntity.accepted().build();
    }

    /**
     * Deletes an event.
     * This endpoint allows event hosts to remove events.
     *
     * @param authentication The authentication object containing the current user's details
     * @param eventId The ID of the event to delete
     * @return ResponseEntity with HTTP 204 status if deletion is successful
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(Authentication authentication, @PathVariable final Long eventId) {
        var username = authentication.getName();
        eventService.deleteEvent(eventId, username);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggles a user's attendance status for an event.
     * This endpoint allows users to indicate whether they are attending an event or not.
     *
     * @param authentication The authentication object containing the current user's details
     * @param eventId The ID of the event
     * @param dto The attendance status data
     * @return ResponseEntity with HTTP 202 status if the operation is successful
     */
    @PatchMapping("/{eventId}/status")
    public ResponseEntity<Void> toggleEventAttendance(Authentication authentication,
                                                      @PathVariable final Long eventId,
                                                      @RequestBody final EventStatusDto dto) {
        var username = authentication.getName();
        eventService.toggleEventAttendance(username, eventId, dto);
        return ResponseEntity.accepted().build();
    }

}
