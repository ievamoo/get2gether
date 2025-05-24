package get2gether.service;

import get2gether.TestData;
import get2gether.dto.EventDto;
import get2gether.dto.EventStatusDto;
import get2gether.enums.EventAction;
import get2gether.event.EventPublisher;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.mapper.EventMapper;
import get2gether.model.Event;
import get2gether.model.Group;
import get2gether.enums.Type;
import get2gether.model.User;
import get2gether.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private GroupService groupService;
    @Mock
    private EventMapper eventMapper;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private UserService userService;
    @Mock
    private InviteService inviteService;

    @InjectMocks
    private EventService testEventService;

    private EventDto eventDto;
    private Group group;
    private Event event;
    private Event savedEvent;
    private User host;
    private User newMember;

    @BeforeEach
    void setUp() {
        // Initialize test data
        eventDto = TestData.getEventDto();
        group = TestData.getGroupWithoutId();
        event = TestData.getEvent();
        savedEvent = TestData.getSavedEvent();
        host = TestData.getTestUser();
        newMember = TestData.getNotHostUser();

        // Set up relationships
        savedEvent.setGroup(group);
        savedEvent.setHostUsername(host.getUsername());
        savedEvent.setGoingMembers(new HashSet<>(Set.of(host)));
        group.setMembers(new HashSet<>(Set.of(host)));
        group.setAdmin(host);
    }

    @Test
    void createEvent_shouldSaveEventAndPublishEvents_whenValidInput() {
        when(groupService.findByName(eventDto.getGroupName())).thenReturn(group);
        when(eventMapper.dtoToModel(eventDto)).thenReturn(event);
        when(userService.getUserFromDb("test@gmail.com")).thenReturn(host);
        when(eventRepository.save(event)).thenReturn(savedEvent);
        when(eventMapper.modelToDtoOnGet(savedEvent)).thenReturn(eventDto);

        var result = testEventService.createEvent(eventDto, "test@gmail.com");

        assertNotNull(result);
        assertEquals("Liverpool's parade", result.getName());
        assertEquals(savedEvent.getName(), result.getName());
        assertEquals(savedEvent.getDescription(), result.getDescription());
        assertEquals(savedEvent.getDate(), result.getDate());
        
        verify(eventPublisher).publishEventAction(EventAction.CREATED, savedEvent);
        verify(eventPublisher).publishEventAttendanceChanged(savedEvent, savedEvent.getDate(), host, true);
    }

    @Test
    void createEvent_shouldThrowForbiddenActionException_whenDateIsInPast() {
        var pastEvent = TestData.getPastEventDto();
        assertThrows(ForbiddenActionException.class, () ->
                testEventService.createEvent(pastEvent, "test@gmail.com"));
    }

    @Test
    void updateEvent_shouldUpdateNameAndDescription_whenUserIsHost() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));
        doNothing().when(eventMapper).updateEvent(any(EventDto.class), any(Event.class));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        testEventService.updateEvent(eventDto, "test@gmail.com", 1L);

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_shouldThrowForbiddenActionException_whenUserIsNotHost() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));

        assertThrows(ForbiddenActionException.class, () ->
                testEventService.updateEvent(eventDto, "coco@gmail.com", 1L));
    }

    @Test
    void deleteEvent_shouldDeleteEvent_whenUserIsHost() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));
        doNothing().when(eventRepository).deleteById(1L);
        doNothing().when(eventPublisher).publishEventAction(EventAction.DELETED, savedEvent);

        testEventService.deleteEvent(1L, "test@gmail.com");

        verify(eventRepository).deleteById(1L);
        verify(eventPublisher).publishEventAction(EventAction.DELETED, savedEvent);
    }

    @Test
    void deleteEvent_shouldThrowForbiddenActionException_whenUserIsNotHost() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));

        assertThrows(ForbiddenActionException.class, () ->
                testEventService.deleteEvent(1L, "not.host@gmail.com"));
    }

    @Test
    void getEventByIdFromDb_shouldReturnEvent_whenExists() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));

        var result = testEventService.getEventByIdFromDb(1L);

        assertNotNull(result);
        assertEquals(savedEvent, result);
    }

    @Test
    void getEventByIdFromDb_shouldThrowResourceNotFoundException_whenEventDoesNotExist() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                testEventService.getEventByIdFromDb(1L));
    }

    @Test
    void addUserToEvent_shouldAddUserAndPublishAttendanceChangedEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        doNothing().when(eventPublisher).publishEventAttendanceChanged(savedEvent, savedEvent.getDate(), newMember, true);

        testEventService.addUserToEvent(1L, newMember);

        assertTrue(newMember.getGoingEvents().contains(savedEvent));
        assertTrue(savedEvent.getGoingMembers().contains(newMember));

        verify(eventRepository).save(savedEvent);
        verify(eventPublisher).publishEventAttendanceChanged(savedEvent, savedEvent.getDate(), newMember, true);
    }

    @Test
    void removeUserFromEvent_shouldRemoveUserFromEventAndUserLists() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        testEventService.removeUserFromEvent(1L, newMember);

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void toggleEventAttendance_shouldAddUser_whenIsGoingTrue() {
        when(userService.getUserFromDb("test@gmail.com")).thenReturn(newMember);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        when(inviteService.findByReceiverAndTypeAndTypeId(any(), any(), any())).thenReturn(Optional.empty());
        doNothing().when(eventPublisher).publishEventAttendanceChanged(any(), any(), any(), any());

        var statusDto = new EventStatusDto(true);
        testEventService.toggleEventAttendance("test@gmail.com", 1L, statusDto);

        verify(eventRepository).save(any(Event.class));
        verify(eventPublisher).publishEventAttendanceChanged(any(), any(), any(), any());
    }

    @Test
    void toggleEventAttendance_shouldRemoveUser_whenIsGoingFalse() {
        when(userService.getUserFromDb("test@gmail.com")).thenReturn(newMember);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        when(inviteService.findByReceiverAndTypeAndTypeId(any(), any(), any())).thenReturn(Optional.empty());

        var statusDto = new EventStatusDto(false);
        testEventService.toggleEventAttendance("test@gmail.com", 1L, statusDto);

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void toggleEventAttendance_shouldDeletePendingInvite_whenInviteExists() {
        var invite = TestData.getInvite();
        when(userService.getUserFromDb("test@gmail.com")).thenReturn(newMember);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        when(inviteService.findByReceiverAndTypeAndTypeId(newMember, Type.EVENT, 1L)).thenReturn(Optional.of(invite));
        doNothing().when(inviteService).deleteInvite(any());

        var statusDto = new EventStatusDto(true);
        testEventService.toggleEventAttendance("test@gmail.com", 1L, statusDto);

        verify(inviteService).deleteInvite(any());
    }

    @Test
    void createEvent_shouldPublishEvents() {
        when(groupService.findByName(eventDto.getGroupName())).thenReturn(group);
        when(eventMapper.dtoToModel(eventDto)).thenReturn(event);
        when(userService.getUserFromDb("test@gmail.com")).thenReturn(host);
        when(eventRepository.save(event)).thenReturn(savedEvent);
        when(eventMapper.modelToDtoOnGet(savedEvent)).thenReturn(eventDto);
        doNothing().when(eventPublisher).publishEventAction(any(EventAction.class), any(Event.class));
        doNothing().when(eventPublisher).publishEventAttendanceChanged(any(Event.class), any(LocalDate.class), any(User.class), anyBoolean());

        testEventService.createEvent(eventDto, "test@gmail.com");

        verify(eventPublisher).publishEventAction(eq(EventAction.CREATED), any(Event.class));
        verify(eventPublisher).publishEventAttendanceChanged(any(Event.class), any(LocalDate.class), any(User.class), anyBoolean());
    }

    @Test
    void deleteEvent_shouldPublishEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));
        doNothing().when(eventRepository).deleteById(1L);
        doNothing().when(eventPublisher).publishEventAction(any(EventAction.class), any(Event.class));

        testEventService.deleteEvent(1L, "test@gmail.com");

        verify(eventRepository).deleteById(1L);
        verify(eventPublisher).publishEventAction(eq(EventAction.DELETED), any(Event.class));
    }
}