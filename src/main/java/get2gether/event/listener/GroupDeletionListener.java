package get2gether.event.listener;

import get2gether.event.GroupDeletedEvent;
import get2gether.model.Type;
import get2gether.model.User;
import get2gether.repository.InviteRepository;
import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupDeletionListener {

    private final InviteService inviteService;
    private final InviteRepository inviteRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles the group deletion event by cleaning up related invites and notifying users.
     * This method:
     * 1. Deletes all pending group invites
     * 2. Deletes all event invites for events in the deleted group
     * 3. Notifies all affected users (except the group admin) about the group deletion
     *
     * @param event the event containing information about the deleted group
     */
    @EventListener
    @Transactional
    //pasidebugint
    public void handleGroupDeletionEvent(GroupDeletedEvent event) {
        var groupInvitesToDelete = inviteService.getInvitesByTypeAndTypeId(Type.GROUP, event.getDeletedGroup().getId());
        log.info("[GroupDeletionListener]: {}", groupInvitesToDelete.size());
        inviteRepository.deleteAll(groupInvitesToDelete);

        log.info("[GroupDeletionListener]: group invites deleted for group id {} ", event.getDeletedGroup().getId());
        var pendingInvitesReceivers = groupInvitesToDelete.stream().map(invite -> invite.getReceiver().getUsername()).collect(Collectors.toSet());
        pendingInvitesReceivers.addAll(event.getDeletedGroup().getMembers().stream()
                .map(User::getUsername).collect(Collectors.toSet()));

        event.getDeletedGroup().getEvents().stream()
                .map(event1 -> inviteService.getInvitesByTypeAndTypeId(Type.EVENT, event1.getId()))
                .forEach(invites -> {
                    for (var invite : invites) {
                        var receiver = invite.getReceiver();
                        if (receiver != null) {
                            receiver.getInvitesReceived().remove(invite);
                            pendingInvitesReceivers.add(receiver.getUsername());
                            log.info(receiver.getUsername());
                        }
                        invite.setReceiver(null);
                    }
                    inviteService.deleteInvite(invites);
                });
        log.info("[GroupDeletionListener]: event invites deleted for group id {} ", event.getDeletedGroup().getId());

        pendingInvitesReceivers.stream()
                .filter(receiverUsername -> !Objects.equals(receiverUsername, event.getDeletedGroup().getAdmin().getUsername()))
                .forEach(receiverUsername -> {
                    log.info("Current user {} ", receiverUsername);
                    messagingTemplate.convertAndSendToUser(receiverUsername, "/queue/group-deleted", String.valueOf(event.getDeletedGroup().getId()));
                });
    }

}
