package get2gether.service;

import get2gether.dto.EventDto;
import get2gether.dto.EventStatusDto;
import get2gether.event.*;
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

    @Transactional
    public EventDto createEvent(EventDto eventDto, String username) {
        checkIfDateValid(eventDto.getDate());
        var group = groupService.findByName(eventDto.getGroupName());
        var event = eventMapper.dtoToModel(eventDto);
        var host = userService.getUserFromDb(username);
        event.setGroup(group).setHostUsername(username).setGoingMembers(Set.of(host));
        var savedEvent = eventRepository.save(event);
        eventPublisher.publishEventCreatedEvent(new EventCreatedEvent(this, savedEvent));
        eventPublisher.publishEventAttendanceChangedEvent(new EventAttendanceChangedEvent(this, savedEvent.getDate(), host, true));
        return eventMapper.modelToDtoOnGet(savedEvent);
    }

    private void checkIfDateValid(LocalDate eventDate ) {
        if (eventDate.isBefore(LocalDate.now())) {
            throw new ForbiddenActionException("Event date cannot be in the past");
        }
    }

    @Transactional
    public void updateEvent(EventDto eventDto, String username, Long eventId) {
        var event = getEventByIdFromDb(eventId);
        checkIfUserIsAHost(username, event.getHostUsername());
        eventMapper.updateEvent(eventDto, event);
        var savedEvent = eventRepository.save(event);
        log.info("updated name: {}, updated description: {}", savedEvent.getName(), savedEvent.getDescription());
    }

    @Transactional
    public void deleteEvent(Long eventId, String username) {
        var event = getEventByIdFromDb(eventId);
        checkIfUserIsAHost(username, event.getHostUsername());
        eventRepository.deleteById(eventId);
        eventPublisher.publishEventDeletedEvent(new EventDeletedEvent(this, event));
    }

    public Event getEventByIdFromDb(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.EVENT, "id: " + eventId));
    }

    private static void checkIfUserIsAHost(String username, String eventHost) {
        if (!eventHost.equals(username)) {
            throw new ForbiddenActionException("You are not allowed to edit Event details.");
        }
    }

    public void addUserToEvent(Long typeId, User user) {
        var event = getEventByIdFromDb(typeId);
        user.getGoingEvents().add(event);
        event.getGoingMembers().add(user);
        eventRepository.save(event);
        eventPublisher.publishEventAttendanceChangedEvent(new EventAttendanceChangedEvent(this, event.getDate(), user, true));
        log.info("[EventService]: added {} to event", user.getUsername());
    }

    public void removeUserFromEvent(Long typeId, User receiver) {
        var event = getEventByIdFromDb(typeId);
        event.getGoingMembers().remove(receiver);
        receiver.getGoingEvents().remove(event);
        eventRepository.save(event);
        log.info("[EventService]: removed {} to event", receiver.getUsername());
    }

    public void toggleEventAttendance(String username, Long eventId, EventStatusDto dto) {
        var user = userService.getUserFromDb(username);
        checkForPendingInvites(eventId, user);
        if (dto.getIsGoing()) {
            addUserToEvent(eventId, user);
        } else {
            removeUserFromEvent(eventId, user);
        }

    }

    private void checkForPendingInvites(Long eventId, User user) {
        var pendingEventInvite = inviteService.findByReceiverAndTypeAndTypeId(user, Type.EVENT, eventId);
        pendingEventInvite.ifPresent(invite -> inviteService.deleteInvite(List.of(invite)));
    }
}
