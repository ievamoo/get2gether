package get2gether.service;

import get2gether.dto.InviteDto;
import get2gether.event.EventPublisher;
import get2gether.event.InviteStatusChangedEvent;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.manualMapper.ManualInviteMapper;
import get2gether.model.*;
import get2gether.repository.GroupRepository;
import get2gether.repository.InviteRepository;
import get2gether.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class InviteService {

    private final InviteRepository inviteRepository;
    private final UserService userService;
    private final ManualInviteMapper manualInviteMapper;
    private final GroupService groupService;
    private final EventService eventService;
    private final EventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;


    @Transactional
    public String createNewInviteWhenGroupAlreadyExists(InviteDto inviteDto, String senderName) {
        if (inviteDto.getType() != Type.GROUP) {
            throw new ForbiddenActionException("Only GROUP invites are supported.");
        }

        var group = groupService.getGroupByIdFromDb(inviteDto.getTypeId());
        var sender = userService.getUserFromDb(senderName);

        if (!group.getMembers().contains(sender)) {
            throw new ForbiddenActionException("Only group members can send invites.");
        }

        Map<String, String> errorMessages = new HashMap<>();
        Set<User> receivers = new HashSet<>();

        inviteDto.getReceiverUsernames().forEach(username ->
                userRepository.findByUsername(username)
                        .ifPresentOrElse(
                                receivers::add,
                                () -> errorMessages.put(username, "User does not exist")
                        )
        );

        receivers.forEach(receiver -> processGroupInviteCreation(group, receiver, sender.getUsername(), errorMessages));

//        errorMessages.putAll(createInvitesForGroup(group, receivers, senderName));

        return errorMessages.isEmpty()
                ? "Invite(s) were sent successfully"
                : errorMessages.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    private void processGroupInviteCreation(Group group, User receiver, String senderName, Map<String, String> errorMessages) {
        checkIfInviteShouldBeSent(group, receiver, errorMessages);
        var createdInvite = manualInviteMapper.dtoToModel(group.getId(), receiver, senderName, group.getName());
        inviteRepository.save(createdInvite);
        log.info("[GroupService]: invites created for selected members.");
    }

    private void checkIfInviteShouldBeSent(Group group, User receiver, Map<String, String> errorMessages) {
        if (group.getMembers().contains(receiver)) {
            errorMessages.put(receiver.getUsername(), String.format("User already exists in group %s", group.getName()));
            return;
        }
        if (inviteRepository.existsByReceiverAndTypeAndTypeId(receiver, Type.GROUP, group.getId())) {
            errorMessages.put(receiver.getUsername(), "User is already invited to the group");
        }
    }

    //    private Map<String, String> createInvitesForGroup(Group group, Set<User> receivers, String senderUsername) {
//        Map<String, String> errorMessages = new HashMap<>();
//        receivers.forEach(receiver -> tryCreateGroupInvite(group, receiver, senderUsername, errorMessages));
//
//        return errorMessages;
//    }


    @Transactional
    public List<InviteDto> createInvitesOnGroupCreation(Group group, Set<String> invitedUsernames) {
        var sender = group.getAdmin();

        var receivers = invitedUsernames.stream()
                .map(userRepository::findByUsername)
                .filter(Optional::isPresent)
                .filter(user -> !Objects.equals(user.get().getUsername(), sender.getUsername()))
                .map(Optional::get)
                .collect(Collectors.toSet());

        var inviteDtos = new ArrayList<InviteDto>();

        receivers.forEach(receiver -> {
            var createdInvite = manualInviteMapper.dtoToModel(group.getId(), receiver, sender.getUsername(), group.getName());
            var savedInvite = inviteRepository.save(createdInvite);
            inviteDtos.add(manualInviteMapper.modelToDto(savedInvite));
            log.info("[GroupService]: invite created for member {}", receiver.getUsername());
        });

        return inviteDtos;
    }

    public void handleInviteResponse(String username, Long inviteId, boolean accepted) {
        var user = userService.getUserFromDb(username);
        var existingInvite = getInvite(inviteId, user);
        user.getInvitesReceived().remove(existingInvite);
        inviteRepository.delete(existingInvite);
        log.info("Invite deleted: {}", existingInvite.getId());
        eventPublisher.publishInviteStatusChangedEvent(new InviteStatusChangedEvent(this, existingInvite, accepted));
    }

    private Invite getInvite(Long inviteId, User user) {
        return user.getInvitesReceived().stream()
                .filter(invite -> Objects.equals(invite.getId(), inviteId))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.INVITE, "id: " + inviteId));
    }

    public List<Invite> getInvitesByTypeAndTypeId(Type type, Long id) {
        return inviteRepository.findByTypeAndTypeId(type, id);
    }

    public void deleteInvite(List<Invite> invites) {
         invites.forEach(inviteRepository::delete);
    }

}
