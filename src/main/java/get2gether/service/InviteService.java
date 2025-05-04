package get2gether.service;

import get2gether.dto.InviteDto;
import get2gether.exception.ResourceAlreadyExistsException;
import get2gether.manualMapper.ManualInviteMapper;
import get2gether.model.*;
import get2gether.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final UserService userService;
    private final ManualInviteMapper manualInviteMapper;

    @Transactional
    public String createNewInvite(InviteDto inviteDto, String username) {
        var receiver = userService.getUserFromDb(inviteDto.getReceiverUsername());
        receiver.getInvitesReceived().stream()
                .filter(invite -> invite.getType() == inviteDto.getType()
                        && Objects.equals(invite.getTypeId(), inviteDto.getId()))
                .findAny()
                .ifPresent(invite -> {
                    throw new ResourceAlreadyExistsException(
                            ResourceType.INVITE,
                            "event " + invite.getTypeId() + " for this receiver: " + receiver.getUsername()
                    );
                });
        inviteRepository.save(manualInviteMapper.dtoToModel(inviteDto, receiver).setStatus(InviteStatus.PENDING));
        return String.format("Invite to join your %s was sent to %s", inviteDto.getType(), receiver.getUsername());
    }

    public Invite createInvite (Type type, Long eventId, String senderUsername, User receiver, String typeName) {
        return Invite.builder()
                .type(type)
                .typeId(eventId)
                .typeName(typeName)
                .senderUsername(senderUsername)
                .receiver(receiver)
                .status(InviteStatus.PENDING)
                .build();
    }
}
