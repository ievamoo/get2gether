package get2gether.service;

import get2gether.dto.EventDto;
import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.event.EventPublisher;
import get2gether.event.GroupCreatedEvent;
import get2gether.event.GroupDeletedEvent;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceAlreadyExistsException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.exception.UserNotFoundException;
import get2gether.manualMapper.ManualEventMapper;
import get2gether.manualMapper.ManualGroupMapper;
import get2gether.manualMapper.ManualUserMapper;
import get2gether.model.Group;
import get2gether.model.ResourceType;
import get2gether.model.User;
import get2gether.repository.GroupRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;


@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;
    private final ManualGroupMapper manualGroupMapper;
    private final ManualUserMapper manualUserMapper;
    private final ManualEventMapper manualEventMapper;
    private final EventPublisher eventPublisher;
    private final SimpMessagingTemplate simpMessagingTemplate;


    public GroupDto getGroupById(Long id) {
        var foundGroup = getGroupByIdFromDb(id);
        var availableDays = groupAvailableDays(foundGroup.getMembers());
        return manualGroupMapper.modelToDtoOnGet(foundGroup).setGroupAvailability(availableDays);
    }

    @Transactional
    public GroupDto createGroup(String username, GroupDto groupDto) {
        if (groupRepository.existsByName(groupDto.getName())) {
            throw new ResourceAlreadyExistsException(ResourceType.GROUP, "name: " + groupDto.getName());
        }
        var currentUser = userService.getUserFromDb(username);
        var group = manualGroupMapper.dtoToModelOnGroupCreate(groupDto, currentUser);
        var savedGroup = groupRepository.save(group);
        eventPublisher.publishGroupCreatedEvent(new GroupCreatedEvent(this, savedGroup, groupDto.getInvitedUsernames()));
        return manualGroupMapper.modelToDtoOnGroupCreate(savedGroup);
    }

    @Transactional
    public GroupDto updateGroup(GroupDto editedGroup, Long id) {
        var group = getGroupByIdFromDb(id);
        group.setName(editedGroup.getName()).setGroupColor(editedGroup.getGroupColor());
        var updatedGroup = groupRepository.save(group);
        return manualGroupMapper.modelToDtoOnUpdate(updatedGroup);
    }

    @Transactional
    public void deleteGroup(Long id, String username) {
        var group = getGroupByIdFromDb(id);
        checkIfActionAllowed(username, group);
        groupRepository.deleteById(id);
        eventPublisher.publishGroupDeletedEvent(new GroupDeletedEvent(this, group));
    }

    @Transactional
    public void addMember(Long groupId, User receiver) {
        var group = getGroupByIdFromDb(groupId);
        if (group.getMembers().contains(receiver)) {
            throw new ResourceAlreadyExistsException(ResourceType.USER,  "username: " + receiver.getUsername());
        }
        receiver.getGroups().add(group);
        group.getMembers().add(receiver);
        groupRepository.save(group);
        log.info("[GroupService]: adding {} to the group", receiver.getUsername());
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
    public void leaveGroup(Long groupId, String username) {
        var groupToLeave = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        var currentUser = userService.getUserFromDb(username);
        if (!groupToLeave.getMembers().contains(currentUser)) {
            throw new UserNotFoundException("User not found in group with username" + currentUser.getUsername());
        }
        checkIfAdmin(username, groupToLeave);
        groupToLeave.getMembers().remove(currentUser);
        groupRepository.save(groupToLeave);
//        return userService.getUserFromDb(username).getGroups().stream()
//                .map(manualGroupMapper::modelToDtoOnGroupLeave)
//                .collect(Collectors.toSet());
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

    public Group findByName(String groupName) {
        return groupRepository.findByName(groupName)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.GROUP, "name: " + groupName));
    }

}
