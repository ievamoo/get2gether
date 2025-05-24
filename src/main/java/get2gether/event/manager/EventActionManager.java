package get2gether.event.manager;

import get2gether.event.EventActionEvent;
import get2gether.mapper.InviteMapper;
import get2gether.model.Event;
import get2gether.model.Invite;
import get2gether.enums.Type;
import get2gether.model.User;
import get2gether.repository.InviteRepository;
import get2gether.repository.UserRepository;
import get2gether.service.InviteService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventActionManager extends BaseActionManager {
    private final UserRepository userRepository;
    private final InviteMapper inviteMapper;
    private final InviteRepository inviteRepository;

    public EventActionManager(SimpMessagingTemplate messagingTemplate,
                            InviteService inviteService,
                            UserRepository userRepository,
                            InviteMapper inviteMapper,
                            InviteRepository inviteRepository) {
        super(messagingTemplate, inviteService);
        this.userRepository = userRepository;
        this.inviteMapper = inviteMapper;
        this.inviteRepository = inviteRepository;
    }

    @EventListener
    @Transactional
    public void handleEventAction(EventActionEvent event) {
        log.info("[EventActionManager] Handling event action: {} for event: {}", 
            event.getAction(), event.getEvent().getName());

        switch (event.getAction()) {
            case CREATED -> handleEventCreation(event);
            case DELETED -> handleEventDeletion(event);
            case ATTENDANCE_CHANGED -> handleAttendanceChange(event);
        }
    }

    private void handleEventCreation(EventActionEvent event) {
        var members = event.getEvent().getGroup().getMembers();
        var createdEvent = event.getEvent();

        log.info("[EventActionManager] Processing {} members for event", members.size());
        members.stream()
            .filter(user -> !user.getUsername().equalsIgnoreCase(createdEvent.getHostUsername()))
            .forEach(user -> createAndSendEventInvite(createdEvent, user));
    }

    private void handleEventDeletion(EventActionEvent event) {
        var eventInvites = inviteService.getInvitesByTypeAndTypeId(
            Type.EVENT, 
            event.getEvent().getId()
        );
        inviteService.deleteInvite(eventInvites);
        log.info("[EventActionManager]: invites deleted to event id {}", event.getEvent().getId());
        
        var groupMembers = event.getEvent().getGroup().getMembers();
        groupMembers.forEach(member ->
            notifyUser(member.getUsername(), "/queue/event-deleted", 
                String.valueOf(event.getEvent().getGroup().getId())));
    }

    private void handleAttendanceChange(EventActionEvent event) {
        if (event.getIsGoing()) {
            var user = event.getUser();
            user.getAvailableDays().remove(event.getEventDate());
            userRepository.save(user);
            log.info("[EventActionManager] Available days updated. Date removed: {}", 
                event.getEventDate());
        }
    }

    private void createAndSendEventInvite(Event event, User user) {
        var invite = Invite.builder()
            .type(Type.EVENT)
            .typeId(event.getId())
            .typeName(event.getName())
            .senderUsername(event.getHostUsername())
            .receiver(user)
            .build();

        user.getInvitesReceived().add(invite);
        invite.setReceiver(user);
        var savedInvite = inviteRepository.save(invite);
        log.info("[EventActionManager] Invite saved for user: {}", user.getUsername());

        var inviteDto = inviteMapper.modelToDto(savedInvite);
        notifyUser(user.getUsername(), "/queue/invites", inviteDto);
    }
} 