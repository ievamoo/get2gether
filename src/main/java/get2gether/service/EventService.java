package get2gether.service;

import get2gether.dto.EventDto;
import get2gether.dto.EventStatusDto;
import get2gether.enums.EventAction;
import get2gether.enums.ResourceType;
import get2gether.enums.Type;
import get2gether.event.EventPublisher;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.mapper.EventMapper;
import get2gether.model.*;
import get2gether.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Service responsible for managing events within groups.
 * Handles event lifecycle operations including creation, updates, deletion, and attendance management.
 * Provides functionality for:
 * - Event creation with date validation and host assignment
 * - Event updates with host permission checks
 * - Event deletion with proper cleanup
 * - Attendance management (adding/removing members)
 * - Real-time notifications through event publishing
 * Integrates with group service for group validation and invite service for attendance management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final GroupService groupService;
    private final EventMapper eventMapper;
    private final EventPublisher eventPublisher;
    private final UserService userService;
    private final InviteService inviteService;

    /**
     * Creates a new event in the system.
     * This method performs the following operations:
     * 1. Validates that the event date is not in the past
     * 2. Associates the event with the specified group
     * 3. Sets the host and initializes the going members list
     * 4. Saves the event to the database
     * 5. Publishes event creation and attendance notifications
     *
     * @param eventDto The event data transfer object containing all necessary event details
     * @param username The username of the user who will be the event host
     * @return EventDto containing the created event details
     * @throws ForbiddenActionException if the event date is in the past
     */
    @Transactional
    public EventDto createEvent(EventDto eventDto, String username) {
        checkIfDateValid(eventDto.getDate());
        var group = groupService.findByName(eventDto.getGroupName());
        var event = eventMapper.dtoToModel(eventDto);
        var host = userService.getUserFromDb(username);
        event.setGroup(group).setHostUsername(username).setGoingMembers(Set.of(host));
        var savedEvent = eventRepository.save(event);
        log.info("[EventService]: Created new event '{}' by host {}", savedEvent.getName(), username);
        eventPublisher.publishEventAction(EventAction.CREATED, savedEvent);
        eventPublisher.publishEventAttendanceChanged(savedEvent, savedEvent.getDate(), host, true);
        return eventMapper.modelToDtoOnGet(savedEvent);
    }

    /**
     * Validates that an event date is not in the past.
     * This is a critical validation to ensure events can only be created for future dates.
     *
     * @param eventDate The date to validate
     * @throws ForbiddenActionException if the date is in the past
     */
    private void checkIfDateValid(LocalDate eventDate) {
        if (eventDate.isBefore(LocalDate.now())) {
            log.warn("[EventService]: Attempted to create event with past date: {}", eventDate);
            throw new ForbiddenActionException("Event date cannot be in the past");
        }
    }

    /**
     * Updates an existing event's details.
     * This method performs the following operations:
     * 1. Verifies that the user attempting the update is the event host
     * 2. Updates the event details using the provided DTO
     * 3. Saves the updated event to the database
     * 4. Logs the update operation
     *
     * @param eventDto The updated event data containing the new details
     * @param username The username of the user attempting to update the event
     * @param eventId  The unique identifier of the event to update
     * @throws ForbiddenActionException  if the user is not the event host
     * @throws ResourceNotFoundException if the event is not found
     */
    @Transactional
    public void updateEvent(EventDto eventDto, String username, Long eventId) {
        var event = getEventByIdFromDb(eventId);
        checkIfUserIsAHost(username, event.getHostUsername());
        eventMapper.updateEvent(eventDto, event);
        var savedEvent = eventRepository.save(event);
        log.info("[EventService]: Event '{}' updated by host {}, updated name: {}, updated description: {}",
                savedEvent.getName(), username, savedEvent.getName(), savedEvent.getDescription());
    }

    /**
     * Deletes an event from the system.
     * This method performs the following operations:
     * 1. Verifies that the user attempting deletion is the event host
     * 2. Removes the event from the database
     * 3. Publishes an event deleted notification
     *
     * @param eventId  The unique identifier of the event to delete
     * @param username The username of the user attempting to delete the event
     * @throws ForbiddenActionException  if the user is not the event host
     * @throws ResourceNotFoundException if the event is not found
     */
    @Transactional
    public void deleteEvent(Long eventId, String username) {
        var event = getEventByIdFromDb(eventId);
        checkIfUserIsAHost(username, event.getHostUsername());
        eventRepository.deleteById(eventId);
        log.info("[EventService]: Event '{}' deleted by host {}", event.getName(), username);
        eventPublisher.publishEventAction(EventAction.DELETED, event);
    }

    /**
     * Retrieves an event by its unique identifier.
     * This is a core method used by other service methods to ensure event existence.
     *
     * @param eventId The unique identifier of the event to retrieve
     * @return The found event entity
     * @throws ResourceNotFoundException if the event is not found
     */
    public Event getEventByIdFromDb(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("[EventService]: Attempted to access non-existent event with id: {}", eventId);
                    return new ResourceNotFoundException(ResourceType.EVENT, "id: " + eventId);
                });
    }

    /**
     * Verifies that a user has host privileges for an event.
     * This is a security check to ensure only event hosts can modify event details.
     *
     * @param username  The username of the user attempting the action
     * @param eventHost The username of the event host
     * @throws ForbiddenActionException if the user is not the event host
     */
    private static void checkIfUserIsAHost(String username, String eventHost) {
        if (!eventHost.equals(username)) {
            log.warn("[EventService]: Unauthorized attempt to modify event by non-host user: {}", username);
            throw new ForbiddenActionException("You are not allowed to edit Event details.");
        }
    }

    /**
     * Adds a user to an event's attendance list.
     * This method performs the following operations:
     * 1. Adds the event to the user's going events list
     * 2. Adds the user to the event's going members list
     * 3. Saves the updated event
     * 4. Publishes an attendance changed notification
     *
     * @param typeId The unique identifier of the event
     * @param user   The user to add to the event
     */
    public void addUserToEvent(Long typeId, User user) {
        var event = getEventByIdFromDb(typeId);
        user.getGoingEvents().add(event);
        event.getGoingMembers().add(user);
        eventRepository.save(event);
        log.info("[EventService]: User {} added to event '{}'", user.getUsername(), event.getName());
        eventPublisher.publishEventAttendanceChanged(event, event.getDate(), user, true);
    }

    /**
     * Removes a user from an event's attendance list.
     * This method performs the following operations:
     * 1. Removes the event from the user's going events list
     * 2. Removes the user from the event's going members list
     * 3. Saves the updated event
     *
     * @param typeId   The unique identifier of the event
     * @param receiver The user to remove from the event
     */
    public void removeUserFromEvent(Long typeId, User receiver) {
        var event = getEventByIdFromDb(typeId);
        event.getGoingMembers().remove(receiver);
        receiver.getGoingEvents().remove(event);
        eventRepository.save(event);
        log.info("[EventService]: User {} removed from event '{}'", receiver.getUsername(), event.getName());
    }

    /**
     * Toggles a user's attendance status for an event.
     * This method performs the following operations:
     * 1. Checks for and removes any pending invites
     * 2. Either adds or removes the user from the event based on the status
     * 3. Updates the event's member list accordingly
     *
     * @param username The username of the user whose attendance is being toggled
     * @param eventId  The unique identifier of the event
     * @param dto      The attendance status DTO indicating whether the user is going or not
     */
    public void toggleEventAttendance(String username, Long eventId, EventStatusDto dto) {
        var user = userService.getUserFromDb(username);
        checkForPendingInvites(eventId, user);
        if (dto.getIsGoing()) {
            addUserToEvent(eventId, user);
        } else {
            removeUserFromEvent(eventId, user);
        }
    }

    /**
     * Checks for and removes any pending invites for a user and event.
     * This is a helper method to ensure clean state when toggling attendance.
     *
     * @param eventId The unique identifier of the event
     * @param user    The user to check invites for
     */
    private void checkForPendingInvites(Long eventId, User user) {
        var pendingEventInvite = inviteService.findByReceiverAndTypeAndTypeId(user, Type.EVENT, eventId);
        pendingEventInvite.ifPresent(invite -> {
            inviteService.deleteInvite(List.of(invite));
            log.info("[EventService]: Deleted pending invite for user {} to event {}", user.getUsername(), eventId);
        });
    }
}
