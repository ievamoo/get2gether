package get2gether.service;

import get2gether.dto.EventDto;
import get2gether.dto.EventStatusDto;
import get2gether.event.EventCreatedEvent;
import get2gether.event.EventPublisher;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.manualMapper.ManualEventMapper;
import get2gether.model.*;
import get2gether.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final GroupService groupService;
    private final ManualEventMapper manualEventMapper;
    private final EventPublisher eventPublisher;
    private final UserService userService;

    @Transactional
    public EventDto createEvent(EventDto eventDto, String username) {
        var group = groupService.findByName(eventDto.getGroupName());
        var event = manualEventMapper.dtoToModel(eventDto);
        event.setGroup(group).setHostUsername(username);
        var savedEvent = eventRepository.save(event);
        eventPublisher.publishEventCreatedEvent(new EventCreatedEvent(this, savedEvent));
//        event.setGroup(group).setHostUsername(username);
        return manualEventMapper.modelToDtoOnGet(savedEvent);
    }

    @Transactional
    public EventDto updateEvent(EventDto eventDto, String username, Long eventId) {
        var event = getEventByIdFromDb(eventId);
        checkIfUserIsAHost(username, event.getHostUsername());
        manualEventMapper.updateEvent(eventDto, event);
        var savedEvent = eventRepository.save(event);
        return manualEventMapper.modelToDtoOnGet(eventRepository.save(savedEvent));
    }

    @Transactional
    public void deleteEvent(Long eventId, String username) {
        var event = getEventByIdFromDb(eventId);
        checkIfUserIsAHost(username, event.getHostUsername());
        eventRepository.deleteById(eventId);
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
        log.info("[EventService]: added {} to event", user.getUsername());
    }

    public void removeUserFromEvent(Long typeId, User receiver) {
        var event = getEventByIdFromDb(typeId);
        event.getGoingMembers().remove(receiver);
        receiver.getGoingEvents().remove(event);
        eventRepository.save(event);
        log.info("[EventService]: removed {} to event", receiver.getUsername());
    }

    public List<EventDto> toggleEventAttendance(String username, Long eventId, EventStatusDto dto) {
        var user = userService.getUserFromDb(username);
        if (dto.getIsGoing()) {
            addUserToEvent(eventId, user);
        } else {
            removeUserFromEvent(eventId, user);
        }
        return userService.getUserFromDb(username).getGoingEvents().stream()
                .map(manualEventMapper::modelToDtoOnGet)
                .toList();
    }
}
