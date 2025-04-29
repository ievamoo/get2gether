package get2gether.mapper;

import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.model.Group;
import get2gether.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    @Named("modelToDto")
    @Mapping(source = "members", target = "members", qualifiedByName = "mapToMemberDto")
    GroupDto modelToDto(Group group);

    @Named("dtoToModel")
    @Mapping(source = "members", target = "members", qualifiedByName = "mapToMembers")
    Group dtoToModel(GroupDto dto);

    Group dtoToModelOnGroupCreate(GroupDto dto);

    @Named("mapToMemberDto")
    default Set<UserDto> mapToMemberDto(Set<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .build())
                .collect(Collectors.toSet());
    }

    @Named("mapToMembers")
    default Set<User> mapToMembers(Set<UserDto> userDtos) {
        if (userDtos == null) {
            return null;
        }
        return userDtos.stream()
                .map(dto -> User.builder()
                        .id(dto.getId())
                        .username(dto.getUsername())
                        .build())
                .collect(Collectors.toSet());
    }
}
