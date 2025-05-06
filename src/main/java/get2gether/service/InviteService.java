package get2gether.service;

import get2gether.dto.InviteDto;
import get2gether.event.EventPublisher;
import get2gether.event.InviteStatusChangedEvent;
import get2gether.exception.ResourceAlreadyExistsException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.manualMapper.ManualInviteMapper;
import get2gether.model.*;
import get2gether.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final UserService userService;
    private final ManualInviteMapper manualInviteMapper;
    private final GroupService groupService;
    private final EventService eventService;
    private final EventPublisher eventPublisher;


    @Transactional
    public String createNewInvite(InviteDto inviteDto, String senderName) {
        var receiver = userService.getUserFromDb(inviteDto.getReceiverUsername());
        switch(inviteDto.getType()) {
            case EVENT -> createEventInvite(inviteDto, receiver, senderName);
            case GROUP -> createGroupInvite(inviteDto, receiver, senderName);
        }
        return String.format("Invite to join your %s was sent to %s", inviteDto.getType(), receiver.getUsername());
    }

    private void createGroupInvite(InviteDto inviteDto, User receiver, String senderUsername) {
        var group = groupService.getGroupByIdFromDb(inviteDto.getTypeId());
        var sender = userService.getUserFromDb(senderUsername);
        if (group.getMembers().contains(receiver)) {
            throw new ResourceAlreadyExistsException(ResourceType.USER,
                    String.format("%s in group %s", receiver.getUsername(), group.getName()));
        }
        checkIfInviteAlreadyReceived(inviteDto, receiver);
        var createdInvite = manualInviteMapper.dtoToModel(inviteDto, receiver, sender, group.getName());
        inviteRepository.save(createdInvite);
    }

    private void createEventInvite(InviteDto inviteDto, User receiver, String senderUsername) {
        var event = eventService.getEventByIdFromDb(inviteDto.getTypeId());
        var sender = userService.getUserFromDb(senderUsername);
        checkIfInviteAlreadyReceived(inviteDto, receiver);
        var createdInvite = manualInviteMapper.dtoToModel(inviteDto, receiver, sender, event.getName());
        inviteRepository.save(createdInvite);
    }

    private void checkIfInviteAlreadyReceived(InviteDto inviteDto, User receiver) {
        if (inviteRepository.existsByReceiverAndTypeAndTypeId(receiver, inviteDto.getType(), inviteDto.getTypeId())) {
            throw new ResourceAlreadyExistsException(
                    ResourceType.INVITE,
                    "event " + inviteDto.getTypeId() + " for this receiver: " + receiver.getUsername()
            );
        }
//        receiver.getInvitesReceived().stream()
//                .filter(invite -> invite.getType() == inviteDto.getType()
//                        && Objects.equals(invite.getTypeId(), inviteDto.getTypeId()))
//                .findAny()
//                .ifPresent(invite -> {
//                    throw new ResourceAlreadyExistsException(
//                            ResourceType.INVITE,
//                            "event " + invite.getTypeId() + " for this receiver: " + receiver.getUsername()
//                    );
//                });
    }

    public void changeInviteStatus(String username, Long inviteId, String updatedStatus) {
        var user = userService.getUserFromDb(username);
        var existingInvite = getInvite(inviteId, user);

        existingInvite.setStatus(InviteStatus.valueOf(updatedStatus));
        var  updatedInvite = inviteRepository.save(existingInvite);
        eventPublisher.publishInviteStatusChangedEvent(new InviteStatusChangedEvent(this, updatedInvite));
    }

    private Invite getInvite(Long inviteId, User user) {
        return user.getInvitesReceived().stream()
                .filter(invite -> Objects.equals(invite.getId(), inviteId))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.INVITE, "id: " + inviteId));
    }
}
