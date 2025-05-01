package get2gether.service;

import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.exception.ForbiddenActionException;
import get2gether.manualMapper.ManualGroupMapper;
import get2gether.manualMapper.ManualUserMapper;
import get2gether.mapper.GroupMapper;
import get2gether.model.Group;
import get2gether.repository.GroupRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final UserService userService;
    private final ManualGroupMapper manualGroupMapper;
    private final ManualUserMapper manualUserMapper;

    public GroupDto getGroupById(Long id) {
        var foundGroup = getGroupByIdFromDb(id);
        return manualGroupMapper.modelToDtoOnGet(foundGroup);
    }


    public GroupDto createGroup(String username, GroupDto groupDto) {
        if (groupRepository.existsByName(groupDto.getName())) {
            throw new EntityExistsException("Group already exists by name: " + groupDto.getName());
        }
        var currentUser = userService.getUserFromDb(username);
        var group = groupMapper.dtoToModelOnGroupCreate(groupDto);
        group.setMembers(Set.of(currentUser));
        group.setAdmin(currentUser);
        var savedGroup = groupRepository.save(group);
        return manualGroupMapper.modelToDtoOnGroupCreate(savedGroup);
    }

    public GroupDto updateGroup(GroupDto editedGroup, Long id) {
        var group = getGroupByIdFromDb(id);
        group.setName(editedGroup.getName());
        return manualGroupMapper.modelToDtoOnUpdate(groupRepository.save(group));
    }

    public void deleteGroup(Long id, String username) {
        var group = getGroupByIdFromDb(id);
        if (!group.getAdmin().getUsername().equals(username)) {
            throw new ForbiddenActionException("You are not allowed to delete this group.");
        }
        groupRepository.deleteById(id);
    }

    public Set<UserDto> addMember(Long id, UserDto userDto) {
        var group = getGroupByIdFromDb(id);
        var user = userService.getUserFromDb(userDto.getUsername());
        if (group.getMembers().contains(user)) {
            throw new EntityExistsException("User already exists in this group: " + userDto.getUsername());
        }
        group.getMembers().add(user);
        var savedGroup = groupRepository.save(group);
        return savedGroup.getMembers().stream()
                .map(manualUserMapper::modelToDtoOnGroupCreate)
                .collect(Collectors.toSet());
    }


    private Group getGroupByIdFromDb(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found by id: " + id));
    }
}
