package get2gether.mapper;

import get2gether.dto.InviteDto;
import get2gether.model.Invite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InviteMapper {

    @Mapping(target = "accepted", ignore = true)
    InviteDto modelToDto(Invite invite);

    List<InviteDto> toDtoList(List<Invite> invites);



}
