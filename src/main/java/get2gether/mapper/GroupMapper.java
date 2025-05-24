package get2gether.mapper;

import get2gether.dto.GroupDto;
import get2gether.model.Group;
import get2gether.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper class responsible for converting between Group domain models and GroupDto data transfer objects.
 * Handles the transformation of group data including admin information, member lists, associated events,
 * messages, and group settings. Provides specialized mapping methods for different contexts such as
 * group creation, retrieval, and updates, ensuring proper formatting and data consistency between
 * domain models and DTOs.
 */
@Service
@RequiredArgsConstructor
public class GroupMapper {

    private final UserMapper userMapper;
    private final EventMapper eventMapper;
    private final MessageMapper messageMapper;

    public GroupDto modelToDtoOnGroupCreate(Group group) {
        return GroupDto.builder()
                .id(group.getId())
                .admin(userMapper.modelToDtoOnGroupCreate(group.getAdmin()))
                .name(group.getName())
                .members(group.getMembers().stream()
                        .map(userMapper::modelToDtoOnGroupCreate)
                        .collect(Collectors.toSet()))
                .groupColor(group.getGroupColor())
                .build();
    }

    public GroupDto modelToDtoOnGet(Group group) {
        return GroupDto.builder()
                .id(group.getId())
                .admin(userMapper.modelToDtoOnGroupCreate(group.getAdmin()))
                .name(group.getName())
                .members(group.getMembers().stream()
                        .map(userMapper::modelToDtoOnGroupCreate)
                        .collect(Collectors.toSet()))
                .events(group.getEvents().stream()
                        .map(eventMapper::modelToDtoOnGet)
                        .toList())
                .groupColor(group.getGroupColor())
                .messages(group.getMessages().stream()
                        .map(messageMapper::modelToDto)
                        .toList())
                .build();

    }

    public GroupDto modelToDtoOnUpdate(Group group) {
        return GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .groupColor(group.getGroupColor())
                .build();
    }

    public Group dtoToModelOnGroupCreate(GroupDto dto, User user) {
        return Group.builder()
                .name(dto.getName())
                .admin(user)
                .members(Set.of(user))
                .groupColor(dto.getGroupColor())
                .build();
    }
}
