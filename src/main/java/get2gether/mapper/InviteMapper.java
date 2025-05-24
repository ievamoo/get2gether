package get2gether.mapper;

import get2gether.dto.InviteDto;
import get2gether.enums.ResourceType;
import get2gether.enums.Type;
import get2gether.exception.ResourceNotFoundException;
import get2gether.model.*;
import get2gether.repository.EventRepository;
import get2gether.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

/**
 * Mapper class responsible for converting between Invite domain models and InviteDto data transfer objects.
 * Handles the transformation of invitation data for both group and event invites, including sender information,
 * receiver details, and associated group/event information. Provides methods for creating new invites and
 * converting existing invites to DTOs with properly formatted sender names and additional context information
 * such as group names and event dates.
 */
@Service
@RequiredArgsConstructor
public class InviteMapper {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public Invite dtoToModel(Long groupId, User receiver, String senderUsername, String typeName) {
        return Invite.builder()
                .type(Type.GROUP)
                .typeId(groupId)
                .senderUsername(senderUsername)
                .typeName(typeName)
                .receiver(receiver)
                .build();
    }

    public InviteDto modelToDto(Invite invite) {
        return InviteDto.builder()
                .id(invite.getId())
                .type(invite.getType())
                .typeId(invite.getTypeId())
                .typeName(invite.getTypeName())
                .senderUsername(formatSender(invite.getSenderUsername()))
                .groupName(invite.getType() == Type.EVENT ? getGroupName(invite.getTypeId()) : null)
                .eventDate(invite.getType() == Type.EVENT ? getEventDate(invite.getTypeId()) : null)
                .receiverUsernames(Set.of(invite.getReceiver().getUsername()))
                .build();
    }

    private String formatSender(String senderName) {
        var user = userRepository.findByUsername(senderName).orElseThrow(
                () -> new ResourceNotFoundException(ResourceType.USER, "username:" + senderName));
        return String.format("%s %s", user.getFirstName(), user.getLastName());
    }

    private String getGroupName(Long eventId) {
        var event = getEvent(eventId);
        return event.getGroup().getName();
    }

    private LocalDate getEventDate(Long eventId) {
        var event = getEvent(eventId);
        return event.getDate();
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.EVENT, "id: " + eventId));
    }


}
