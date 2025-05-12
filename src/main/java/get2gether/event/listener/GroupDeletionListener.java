package get2gether.event.listener;

import get2gether.event.GroupDeletedEvent;
import get2gether.model.Type;
import get2gether.repository.InviteRepository;
import get2gether.repository.UserRepository;
import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupDeletionListener {

    private final InviteService inviteService;
    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;

    @EventListener
    @Transactional
    //pasidebugint
    public void handleGroupDeletionEvent(GroupDeletedEvent event) {
        var groupInvitesToDelete = inviteService.getInvitesByTypeAndTypeId(Type.GROUP, event.getDeletedGroup().getId());
        log.info("[GroupDeletionListener]: {}", groupInvitesToDelete.size());
        inviteRepository.deleteAll(groupInvitesToDelete);

        log.info("[GroupDeletionListener]: group invites deleted for group id {} ", event.getDeletedGroup().getId());

        event.getDeletedGroup().getEvents().stream()
                .map(event1 -> inviteService.getInvitesByTypeAndTypeId(Type.EVENT, event1.getId()))
                .forEach(invites -> {
                    for (var invite : invites) {
                        var receiver = invite.getReceiver();
                        if (receiver != null) {
                            receiver.getInvitesReceived().remove(invite);
                        }
                        invite.setReceiver(null);
                    }
                    inviteService.deleteInvite(invites);
                });
        log.info("[GroupDeletionListener]: event invites deleted for group id {} ", event.getDeletedGroup().getId());
    }
}
