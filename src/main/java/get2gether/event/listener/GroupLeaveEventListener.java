package get2gether.event.listener;


import get2gether.event.GroupLeaveEvent;
import get2gether.model.Event;
import get2gether.model.Type;
import get2gether.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupLeaveEventListener {

    private final InviteRepository inviteRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles the group leave event by cleaning up event invites and notifying group members.
     * This method:
     * 1. Gets all event IDs from the group
     * 2. Deletes any pending event invites for the leaving user
     * 3. Notifies all group members about the user leaving
     *
     * @param event the event containing information about the user leaving the group
     */
    @EventListener
    public void handleGroupLeaveEvent(GroupLeaveEvent event) {
        var group = event.getLeftGroup();
        log.info("[GroupLeaveEventListener]: group {}", group.getName());

        var groupEventsId = group.getEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        groupEventsId.forEach(id -> {
            var inviteToDelete = inviteRepository.findByReceiverAndTypeAndTypeId(event.getUser(), Type.EVENT, id);
            if (inviteToDelete.isPresent()) {
                log.info("[GroupLeaveEventListener]: invite ToDelete {}",inviteToDelete.get().getId());
                inviteRepository.delete(inviteToDelete.get());
            }
        });

        messagingTemplate.convertAndSend("/topic/group/" + group.getId(), "User left the group.");
    }

}
