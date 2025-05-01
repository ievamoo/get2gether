package get2gether.manualMapper;

import get2gether.dto.GroupDto;
import get2gether.model.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManualGroupMapper {

    private final ManualUserMapper userMapper;
    private final ManualEventMapper manualEventMapper;

    public GroupDto modelToDtoOnGroupCreate(Group group) {
        return GroupDto.builder()
                .id(group.getId())
                .admin(userMapper.modelToDtoOnGroupCreate(group.getAdmin()))
                .name(group.getName())
                .members(group.getMembers().stream()
                        .map(userMapper::modelToDtoOnGroupCreate)
                        .collect(Collectors.toSet()))
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
                        .map(manualEventMapper::modelToDtoOnGet)
                        .toList())
                .build();

    }

    public GroupDto modelToDtoOnUpdate(Group group) {
        return GroupDto.builder()
                .name(group.getName())
                .build();
    }

    public GroupDto modelToDtoOnGroupLeave(Group group) {
        return GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .build();
    }
}
