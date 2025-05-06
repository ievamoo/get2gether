package get2gether.manualMapper;

import get2gether.dto.InviteDto;
import get2gether.model.Invite;
import get2gether.model.InviteStatus;
import get2gether.model.User;
import org.springframework.stereotype.Service;

@Service
public class ManualInviteMapper {

    public Invite dtoToModel(InviteDto dto, User receiver, User sender, String typeName) {
        return Invite.builder()
                .type(dto.getType())
                .typeId(dto.getTypeId())
                .senderUsername(String.format("%s %s", sender.getFirstName(), sender.getLastName()) )
                .typeName(typeName)
                .status(InviteStatus.PENDING)
                .receiver(receiver)
                .build();
    }

    public InviteDto modelToDto(Invite invite) {
        return InviteDto.builder()
                .id(invite.getId())
                .type(invite.getType())
                .typeId(invite.getTypeId())
                .status(invite.getStatus())
                .senderUsername(invite.getSenderUsername())
                .typeName(invite.getTypeName())
                .build();
    }

}
