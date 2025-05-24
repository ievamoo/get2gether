package get2gether.service;

import get2gether.dto.InviteDto;
import get2gether.event.EventPublisher;
import get2gether.event.InviteStatusChangedEvent;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.mapper.InviteMapper;
import get2gether.model.*;
import get2gether.repository.InviteRepository;
import get2gether.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for managing group and event invites.
 * Handles invite creation, response processing, and notification delivery.
 * Provides functionality for both group and event invitation management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InviteService {

    private final InviteRepository inviteRepository;
    private final UserService userService;
    private final InviteMapper inviteMapper;
    private final GroupService groupService;
    private final EventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Creates new group invites for an existing group.
     * The method:
     * 1. Validates that the sender is a group member
     * 2. Processes each receiver username
     * 3. Creates and sends invites to valid receivers
     * 4. Collects error messages for invalid receivers
     *
     * @param inviteDto the invite information
     * @param senderName the username of the sender
     * @return success message or list of error messages
     * @throws ForbiddenActionException if the sender is not a group member or if non-group invites are attempted
     */
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

        return errorMessages.isEmpty()
                ? "Invite(s) were sent successfully"
                : errorMessages.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Processes the creation of a group invite for a single receiver.
     * The method:
     * 1. Validates if the invite should be sent
     * 2. Creates and saves the invite
     * 3. Sends a WebSocket notification to the receiver
     *
     * @param group the group to invite to
     * @param receiver the user to invite
     * @param senderName the username of the sender
     * @param errorMessages map to collect error messages
     */
    private void processGroupInviteCreation(Group group, User receiver, String senderName, Map<String, String> errorMessages) {
        checkIfInviteShouldBeSent(group, receiver, errorMessages);
        var createdInvite = inviteMapper.dtoToModel(group.getId(), receiver, senderName, group.getName());
        var inviteDto = inviteMapper.modelToDto(inviteRepository.save(createdInvite));
        simpMessagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/invites", inviteDto);
        log.info("[GroupService]: invites created for selected members.");
    }

    /**
     * Validates if an invite should be sent to a user.
     * Checks if the user is already a member or has a pending invite.
     *
     * @param group the group to check
     * @param receiver the user to validate
     * @param errorMessages map to collect error messages
     */
    private void checkIfInviteShouldBeSent(Group group, User receiver, Map<String, String> errorMessages) {
        if (group.getMembers().contains(receiver)) {
            errorMessages.put(receiver.getUsername(), String.format("User already exists in group %s", group.getName()));
            return;
        }
        if (inviteRepository.existsByReceiverAndTypeAndTypeId(receiver, Type.GROUP, group.getId())) {
            errorMessages.put(receiver.getUsername(), "User is already invited to the group");
        }
    }

    /**
     * Creates invites when a new group is created.
     * The method:
     * 1. Filters out invalid usernames and the group admin
     * 2. Creates invites for each valid receiver
     * 3. Saves the invites and collects the DTOs
     *
     * @param group the newly created group
     * @param invitedUsernames set of usernames to invite
     * @return list of created invite DTOs
     */
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
            var createdInvite = inviteMapper.dtoToModel(group.getId(), receiver, sender.getUsername(), group.getName());
            var savedInvite = inviteRepository.save(createdInvite);
            inviteDtos.add(inviteMapper.modelToDto(savedInvite));
            log.info("[GroupService]: invite created for member {}", receiver.getUsername());
        });

        return inviteDtos;
    }

    /**
     * Handles a user's response to an invite.
     * The method:
     * 1. Removes the invite from the user's received invites
     * 2. Deletes the invite from the database
     * 3. Publishes an InviteStatusChangedEvent
     *
     * @param username the username of the user responding
     * @param inviteId the ID of the invite
     * @param accepted whether the invite was accepted
     * @throws ResourceNotFoundException if the invite is not found
     */
    public void handleInviteResponse(String username, Long inviteId, boolean accepted) {
        var user = userService.getUserFromDb(username);
        var existingInvite = getInvite(inviteId, user);
        user.getInvitesReceived().remove(existingInvite);
        inviteRepository.delete(existingInvite);
        log.info("Invite deleted: {}", existingInvite.getId());
        eventPublisher.publishInviteStatusChangedEvent(new InviteStatusChangedEvent(this, existingInvite, accepted));
    }

    /**
     * Retrieves an invite from a user's received invites.
     *
     * @param inviteId the ID of the invite
     * @param user the user to check
     * @return the found invite
     * @throws ResourceNotFoundException if the invite is not found
     */
    private Invite getInvite(Long inviteId, User user) {
        return user.getInvitesReceived().stream()
                .filter(invite -> Objects.equals(invite.getId(), inviteId))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.INVITE, "id: " + inviteId));
    }

    /**
     * Retrieves all invites of a specific type for a given entity.
     *
     * @param type the type of invite (GROUP or EVENT)
     * @param id the ID of the entity
     * @return list of matching invites
     */
    public List<Invite> getInvitesByTypeAndTypeId(Type type, Long id) {
        return inviteRepository.findByTypeAndTypeId(type, id);
    }

    /**
     * Deletes a list of invites.
     *
     * @param invites the invites to delete
     */
    public void deleteInvite(List<Invite> invites) {
        invites.forEach(inviteRepository::delete);
    }

    /**
     * Finds an invite by receiver, type, and entity ID.
     *
     * @param receiver the user who received the invite
     * @param type the type of invite
     * @param typeId the ID of the entity
     * @return Optional containing the found invite, if any
     */
    public Optional<Invite> findByReceiverAndTypeAndTypeId(User receiver, Type type, Long typeId) {
        return inviteRepository.findByReceiverAndTypeAndTypeId(receiver, type, typeId);
    }
}
