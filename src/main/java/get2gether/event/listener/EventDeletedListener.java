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

    @EventListener
    @Transactional
    public void handleEventDeletedEvent(EventDeletedEvent event) {
        var eventInvites = inviteService.getInvitesByTypeAndTypeId(Type.EVENT, event.getDeletedEvent().getId());
        inviteService.deleteInvite(eventInvites);
        log.info("[EventDeletedListener]: invites deleted to event id {}", event.getDeletedEvent().getId());
    }
}
