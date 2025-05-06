package get2gether.event.listener;

import get2gether.event.InviteStatusChangedEvent;
import get2gether.exception.ForbiddenActionException;
import get2gether.model.Invite;
import get2gether.model.InviteStatus;
import get2gether.model.User;
import get2gether.repository.InviteRepository;
import get2gether.service.EventService;
import get2gether.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class InviteStatusListener {

    private final GroupService groupService;
    private final EventService eventService;
    private final InviteRepository inviteRepository;


    @EventListener
    public void handleInviteStatusChange(InviteStatusChangedEvent event) {
        var invite = event.getUpdatedInvite();
        var receiver = invite.getReceiver();
        log.info("[InviteStatusListener]: handling {} invite status change..", invite.getType());
        switch (invite.getType()) {
            case GROUP ->   handleGroupInviteStatusChange(invite, receiver);
            case EVENT -> handleEventInviteStatusChange(invite, receiver);
        }
    }

    private void handleEventInviteStatusChange(Invite invite, User receiver) {
        switch (invite.getStatus()) {
            case ACCEPTED ->  eventService.addToEvent(invite.getTypeId(), receiver);
            case REJECTED -> {
                eventService.removeFromEvent(invite.getTypeId(), receiver);
                deleteInvite(invite);
            }
            default -> log.warn("[InviteStatusListener]: Unexpected status for group invite: {}", invite.getStatus());
        }

        if (invite.getStatus() != InviteStatus.ACCEPTED) {
            throw new ForbiddenActionException("Invalid status" + invite.getStatus());
        }
    }



    //TODO isitikinti, kad tikrai issitrina abipusiai

    private void handleGroupInviteStatusChange(Invite invite, User receiver) {
        switch (invite.getStatus()) {
            case ACCEPTED -> handleAcceptedGroupInvite(invite, receiver);
            case REJECTED -> deleteInvite(invite);
            default -> log.warn("[InviteStatusListener]: Unexpected status for group invite: {}", invite.getStatus());
        }
    }

    private void handleAcceptedGroupInvite(Invite invite, User receiver) {
        groupService.addMember(invite.getTypeId(), receiver);
        deleteInvite(invite);
    }

    private void deleteInvite(Invite invite) {
        inviteRepository.delete(invite);
        log.info("[InviteStatusListener]: Invite (id: {}) deleted.", invite.getId());
    }

}
