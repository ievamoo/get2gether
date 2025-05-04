package get2gether.service;

import get2gether.dto.EventDto;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.manualMapper.ManualEventMapper;
import get2gether.model.*;
import get2gether.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final GroupService groupService;
    private final ManualEventMapper manualEventMapper;
    private final InviteService inviteService;

    @Transactional
    public EventDto createEvent(EventDto eventDto, String username) {
        var group = groupService.getGroupByIdFromDb(eventDto.getGroupId());
        var event = manualEventMapper.dtoToModel(eventDto);
        group.getMembers().forEach(user -> user.getInvitesReceived()
                .add(inviteService.createInvite(Type.EVENT, event.getId(), username, user, event.getName())));
        event.setGroup(group).setHostUsername(username);
        var savedEvent = eventRepository.save(event);
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

    private Event getEventByIdFromDb(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.EVENT, "id: " + eventId));
    }

    private static void checkIfUserIsAHost(String username, String eventHost) {
        if (!eventHost.equals(username)) {
            throw new ForbiddenActionException("You are not allowed to edit Event details.");
        }
    }
}
