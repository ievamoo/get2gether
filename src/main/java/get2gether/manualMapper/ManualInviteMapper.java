package get2gether.manualMapper;

import get2gether.dto.InviteDto;
import get2gether.model.Invite;
import get2gether.model.User;
import org.springframework.stereotype.Service;

@Service
public class ManualInviteMapper {

    public Invite dtoToModel(InviteDto dto, User receiver) {
        return Invite.builder()
                .type(dto.getType())
                .typeId(dto.getTypeId())
                .senderUsername(dto.getSenderUsername())
                .receiver(receiver)
                .build();
    }

    public InviteDto modelToDto(Invite invite) {
        return InviteDto.builder()
                .id(invite.getId())
                .type(invite.getType())
                .typeId(invite.getTypeId())
                .status(invite.getStatus())
                .typeName(invite.getTypeName())
                .build();
    }

}
