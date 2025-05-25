package get2gether.event.manager;

import get2gether.enums.Type;
import get2gether.event.GroupActionEvent;
import get2gether.model.Group;
import get2gether.model.User;
import get2gether.service.InviteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupActionManager extends BaseActionManager {

    public GroupActionManager(SimpMessagingTemplate messagingTemplate,
                              InviteService inviteService) {
        super(messagingTemplate, inviteService);
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
            case AVAILABLE_DAYS_UPDATED -> handleAvailableDaysUpdate(event);
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

        var groupInvites = inviteService.getInvitesByTypeAndTypeId(Type.GROUP, group.getId());
        inviteService.deleteInvite(groupInvites);
        log.info("[GroupActionManager]: group invites deleted for group id {}", group.getId());

        group.getEvents().forEach(event1 -> {
            var eventInvites = inviteService.getInvitesByTypeAndTypeId(Type.EVENT, event1.getId());
            inviteService.deleteInvite(eventInvites);
        });
        log.info("[GroupActionManager]: event invites deleted for group id {}", group.getId());

        var pendingInvitesReceivers = groupInvites.stream()
                .map(invite -> invite.getReceiver().getUsername())
                .collect(Collectors.toSet());

        pendingInvitesReceivers.addAll(group.getMembers().stream()
                .map(User::getUsername)
                .collect(Collectors.toSet()));

        pendingInvitesReceivers.stream()
                .filter(username -> !username.equals(group.getAdmin().getUsername()))
                .forEach(username -> notifyUser(username, "/queue/group-deleted", String.valueOf(group.getId())));
    }

    private void handleGroupLeave(GroupActionEvent event) {
        var group = event.getGroup();
        var user = event.getUser();
        log.info("[GroupActionManager]: handling leave for group {}", group.getName());

        group.getEvents().forEach(event1 -> {
            var inviteToDelete = inviteService.findByReceiverAndTypeAndTypeId(user, Type.EVENT, event1.getId());
            inviteToDelete.ifPresent(invite -> inviteService.deleteInvite(List.of(invite)));
        });

        notifyGroup(group.getId(), "User left the group.");
    }

    private void handleAvailableDaysUpdate(GroupActionEvent event) {
        var user = event.getUser();
        var userGroupIds = user.getGroups().stream()
            .map(Group::getId)
            .toList();

        userGroupIds.forEach(groupId -> {
            notifyGroup(groupId, String.format("Available days updated by user %s", user.getUsername()));
        });
    }
}

