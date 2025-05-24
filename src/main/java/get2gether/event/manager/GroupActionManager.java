package get2gether.event.manager;

import get2gether.event.GroupActionEvent;
import get2gether.enums.Type;
import get2gether.model.User;
import get2gether.repository.InviteRepository;
import get2gether.service.GroupService;
import get2gether.service.InviteService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class GroupActionManager extends BaseActionManager {
    private final GroupService groupService;
    private final InviteRepository inviteRepository;

    public GroupActionManager(SimpMessagingTemplate messagingTemplate, 
                            InviteService inviteService,
                            GroupService groupService,
                            InviteRepository inviteRepository) {
        super(messagingTemplate, inviteService);
        this.groupService = groupService;
        this.inviteRepository = inviteRepository;
    }

    @EventListener
    @Transactional
    public void handleGroupAction(GroupActionEvent event) {
        log.info("[GroupActionManager] Handling group action: {} for group: {}", 
            event.getAction(), event.getGroup().getName());

        switch (event.getAction()) {
            case CREATED -> handleGroupCreation(event);
            case DELETED -> handleGroupDeletion(event);
            case LEAVE -> handleGroupLeave(event);
        }
    }

    private void handleGroupCreation(GroupActionEvent event) {
        if (event.getInvitedUsernames() == null || event.getInvitedUsernames().isEmpty()) {
            log.info("No users were invited on group {} creation", event.getGroup().getName());
            return;
        }
        
        var invitesToSend = inviteService.createInvitesOnGroupCreation(
            event.getGroup(), 
            event.getInvitedUsernames()
        );
        
        invitesToSend.forEach(inviteDto -> {
            var receiverUsername = inviteDto.getReceiverUsernames().iterator().next();
            notifyUser(receiverUsername, "/queue/invites", inviteDto);
        });
    }

    private void handleGroupDeletion(GroupActionEvent event) {
        var group = event.getGroup();
        
        // Clean up group invites
        var groupInvites = inviteService.getInvitesByTypeAndTypeId(Type.GROUP, group.getId());
        inviteRepository.deleteAll(groupInvites);
        log.info("[GroupActionManager]: group invites deleted for group id {}", group.getId());

        // Clean up event invites
        group.getEvents().forEach(event1 -> {
            var eventInvites = inviteService.getInvitesByTypeAndTypeId(Type.EVENT, event1.getId());
            inviteService.deleteInvite(eventInvites);
        });
        log.info("[GroupActionManager]: event invites deleted for group id {}", group.getId());

        // Notify affected users
        var pendingInvitesReceivers = groupInvites.stream()
            .map(invite -> invite.getReceiver().getUsername())
            .collect(Collectors.toSet());
        
        pendingInvitesReceivers.addAll(group.getMembers().stream()
            .map(User::getUsername)
            .collect(Collectors.toSet()));

        pendingInvitesReceivers.stream()
            .filter(receiverUsername -> !receiverUsername.equals(group.getAdmin().getUsername()))
            .forEach(receiverUsername -> 
                notifyUser(receiverUsername, "/queue/group-deleted", String.valueOf(group.getId()))
            );
    }

    private void handleGroupLeave(GroupActionEvent event) {
        var group = event.getGroup();
        var user = event.getUser();
        log.info("[GroupActionManager]: handling leave for group {}", group.getName());

        // Clean up event invites
        group.getEvents().forEach(event1 -> {
            var inviteToDelete = inviteRepository.findByReceiverAndTypeAndTypeId(
                user, 
                Type.EVENT, 
                event1.getId()
            );
            inviteToDelete.ifPresent(inviteRepository::delete);
        });

        notifyGroup(group.getId(), "User left the group.");
    }
} 