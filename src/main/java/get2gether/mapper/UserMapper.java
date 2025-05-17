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

@Mapper(componentModel = "spring", uses = {InviteMapper.class, EventMapper.class, GroupMapper.class})
public interface UserMapper {

    default void updateCurrentUser(UserDto dto, User user) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
    }

    @Named("toDtoOnGroupCreate")
    @Mapping(target = "availableDays", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "invitesReceived", ignore = true)
    @Mapping(target = "goingEvents", ignore = true)
    UserDto toDtoOnGroupCreate(User user);


    @Mapping(target = "groups", expression = "java(mapGroups(user.getGroups()))")
    @Mapping(target = "invitesReceived", source = "invitesReceived")
    @Mapping(target = "goingEvents", source = "goingEvents")
    UserDto modelToDtoOnGetUser(User user);

    default Set<GroupDto> mapGroups(Set<Group> groups) {
        if (groups == null) return null;
        return groups.stream()
                .map(group -> GroupDto.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .groupColor(group.getGroupColor())
                        .build())
                .collect(Collectors.toSet());
    }








}
