package get2gether.mapper;

import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.model.Group;
import get2gether.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AvailabilityMapper.class})
public interface UserMapper {

    @Named("modelToDto")
    @Mapping(source = "availableDays", target = "availableDays", qualifiedByName = "modelToDto")
    @Mapping(source = "groups", target = "groups", qualifiedByName = "mapToGroupDto")
    UserDto modelToDto(User user);

    @Named("dtoToModel")
    @Mapping(source = "availableDays", target = "availableDays", qualifiedByName = "dtoToModel")
    @Mapping(source = "groups", target = "groups", qualifiedByName = "mapToGroup")
    User dtoToModel(UserDto dto);

    default void updateUserProfile(UserDto dto, @MappingTarget User entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
    }

    @Named("mapToGroupDto")
    default Set<GroupDto> mapToGroupDto(Set<Group> groups) {
        if (groups == null) {
            return null;
        }
        return groups.stream()
                .map(group -> GroupDto.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .build())
                .collect(Collectors.toSet());
    }

    @Named("mapToGroup")
    default Set<Group> mapToGroup(Set<GroupDto> groupDtos) {
        if (groupDtos == null) {
            return null;
        }
        return groupDtos.stream()
                .map(dto -> Group.builder()
                        .id(dto.getId())
                        .name(dto.getName())
                        .build())
                .collect(Collectors.toSet());
    }
}
