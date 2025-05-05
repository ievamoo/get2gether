package get2gether.service;

import get2gether.dto.EventDto;
import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.UserNotFoundException;
import get2gether.manualMapper.ManualEventMapper;
import get2gether.manualMapper.ManualGroupMapper;
import get2gether.manualMapper.ManualUserMapper;
import get2gether.mapper.GroupMapper;
import get2gether.model.Group;
import get2gether.model.User;
import get2gether.repository.GroupRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final ManualEventMapper manualEventMapper;

    public GroupDto getGroupById(Long id) {
        var foundGroup = getGroupByIdFromDb(id);
        var availableDays = groupAvailableDays(foundGroup.getMembers());
        return manualGroupMapper.modelToDtoOnGet(foundGroup).setGroupAvailability(availableDays);
    }

    @Transactional
    public GroupDto createGroup(String username, GroupDto groupDto) {
        if (groupRepository.existsByName(groupDto.getName())) {
            throw new EntityExistsException("Group already exists by name: " + groupDto.getName());
        }
        var currentUser = userService.getUserFromDb(username);
        var group = groupMapper.dtoToModelOnGroupCreate(groupDto)
                .setMembers(Set.of(currentUser)).setAdmin(currentUser);
        var savedGroup = groupRepository.save(group);
        return manualGroupMapper.modelToDtoOnGroupCreate(savedGroup);
    }

    @Transactional
    public GroupDto updateGroup(GroupDto editedGroup, Long id) {
        var group = getGroupByIdFromDb(id);
        group.setName(editedGroup.getName());
        return manualGroupMapper.modelToDtoOnUpdate(groupRepository.save(group));
    }

    @Transactional
    public void deleteGroup(Long id, String username) {
        var group = getGroupByIdFromDb(id);
        checkIfActionAllowed(username, group);
        groupRepository.deleteById(id);
    }

    @Transactional
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

    @Transactional
    public Set<UserDto> removeUserFromGroup(Long groupId, String memberToDelete, String username) {
        var group = getGroupByIdFromDb(groupId);
        checkIfAdmin(memberToDelete, group);
        var userToDelete = userService.getUserFromDb(memberToDelete);
        checkIfUserExistsInGroup(group, userToDelete);
        group.getMembers().remove(userToDelete);
        var updatedMemberList = groupRepository.save(group).getMembers();
        return updatedMemberList.stream()
                .map(manualUserMapper::modelToDtoOnGroupCreate)
                .collect(Collectors.toSet());

    }

    @Transactional
    public Set<GroupDto> leaveGroup(Long groupId, String username) {
        var groupToLeave = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        var currentUser = userService.getUserFromDb(username);
        if (!groupToLeave.getMembers().contains(currentUser)) {
            throw new UserNotFoundException("User not found in group with username" + currentUser.getUsername());
        }
        checkIfAdmin(username, groupToLeave);
        groupToLeave.getMembers().remove(currentUser);
        groupRepository.save(groupToLeave);
        return userService.getUserFromDb(username).getGroups().stream()
                .map(manualGroupMapper::modelToDtoOnGroupLeave)
                .collect(Collectors.toSet());
    }

    public List<EventDto> getAllGroupEvents(Long groupId) {
        var group = getGroupByIdFromDb(groupId);
        return group.getEvents().stream()
                .map(manualEventMapper::modelToDtoOnGet).toList();
    }

    public void checkIfUserExistsInGroup(Group group, User userToDelete) {
        if (!group.getMembers().contains(userToDelete)) {
            throw new UserNotFoundException("User not found in group with username" + userToDelete.getUsername());
        }
    }

    public Group getGroupByIdFromDb(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found by id: " + id));
    }

    private void checkIfActionAllowed(String username, Group group) {
        if (!group.getAdmin().getUsername().equals(username)) {
            throw new ForbiddenActionException("You are not allowed to delete this group.");
        }
    }

    private void checkIfAdmin(String username, Group group) {
        if (group.getAdmin().getUsername().equalsIgnoreCase(username)) {
            throw new ForbiddenActionException("You cannot leave the group, dear Admin!");
        }
    }

    private Map<LocalDate, Set<UserDto>> groupAvailableDays(Set<User> members) {
        return members.stream()
                .flatMap(member -> member.getAvailableDays().stream()
                        .map(date -> Map.entry(
                                date,
                                manualUserMapper.modelToDtoOnGroupCreate(member)
                        )))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(
                                Map.Entry::getValue,
                                Collectors.toSet()
                        )
                ));
    }

}
