package get2gether.event.listener;

import get2gether.event.EventDeletedEvent;
import get2gether.model.Type;
import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventDeletedListener {

    private final InviteService inviteService;

    /**
     * Handles the event deletion by removing all related invites.
     * This method:
     * 1. Retrieves all invites associated with the deleted event
     * 2. Deletes all found invites
     *
     * @param event the event containing information about the deleted event
     */
    @EventListener
    @Transactional
    public void handleEventDeletedEvent(EventDeletedEvent event) {
        var eventInvites = inviteService.getInvitesByTypeAndTypeId(Type.EVENT, event.getDeletedEvent().getId());
        inviteService.deleteInvite(eventInvites);
        log.info("[EventDeletedListener]: invites deleted to event id {}", event.getDeletedEvent().getId());
    }
}
