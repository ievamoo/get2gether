package get2gether.service;

import get2gether.dto.EventDto;
import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.event.EventPublisher;
import get2gether.event.GroupCreatedEvent;
import get2gether.event.GroupDeletedEvent;
import get2gether.event.GroupLeaveEvent;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceAlreadyExistsException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.mapper.EventMapper;
import get2gether.mapper.GroupMapper;
import get2gether.mapper.UserMapper;
import get2gether.model.Event;
import get2gether.model.Group;
import get2gether.model.ResourceType;
import get2gether.model.User;
import get2gether.repository.EventRepository;
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

/**
 * Service responsible for managing group-related operations.
 * Handles group creation, updates, deletion, member management, and event coordination.
 * Provides functionality for group availability tracking and member interactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;
    private final GroupMapper groupMapper;
    private final UserMapper userMapper;
    private final EventMapper eventMapper;
    private final EventPublisher eventPublisher;
    private final EventRepository eventRepository;

    /**
     * Retrieves a group by its ID and includes member availability information.
     *
     * @param id the ID of the group to retrieve
     * @return GroupDto containing the group information and member availability
     * @throws EntityNotFoundException if the group is not found
     */
    public GroupDto getGroupById(Long id) {
        var foundGroup = getGroupByIdFromDb(id);
        var availableDays = groupAvailableDays(foundGroup.getMembers());
        return groupMapper.modelToDtoOnGet(foundGroup).setGroupAvailability(availableDays);
    }

    /**
     * Creates a new group and sends invites to selected users.
     * The method:
     * 1. Checks if a group with the same name exists
     * 2. Creates a new group with the current user as admin
     * 3. Saves the group to the database
     * 4. Publishes a GroupCreatedEvent to notify invited users
     *
     * @param username the username of the group creator
     * @param groupDto the group information
     * @return GroupDto containing the created group information
     * @throws ResourceAlreadyExistsException if a group with the same name exists
     */
    @Transactional
    public GroupDto createGroup(String username, GroupDto groupDto) {
        if (groupRepository.existsByName(groupDto.getName())) {
            throw new ResourceAlreadyExistsException(ResourceType.GROUP, "name: " + groupDto.getName());
        }
        var currentUser = userService.getUserFromDb(username);
        var group = groupMapper.dtoToModelOnGroupCreate(groupDto, currentUser);
        var savedGroup = groupRepository.save(group);
        eventPublisher.publishGroupCreatedEvent(new GroupCreatedEvent(this, savedGroup, groupDto.getInvitedUsernames()));
        return groupMapper.modelToDtoOnGroupCreate(savedGroup);
    }

    /**
     * Updates an existing group's information.
     *
     * @param editedGroup the updated group information
     * @param id the ID of the group to update
     * @return GroupDto containing the updated group information
     * @throws EntityNotFoundException if the group is not found
     */
    @Transactional
    public GroupDto updateGroup(GroupDto editedGroup, Long id) {
        var group = getGroupByIdFromDb(id);
        group.setName(editedGroup.getName()).setGroupColor(editedGroup.getGroupColor());
        var updatedGroup = groupRepository.save(group);
        return groupMapper.modelToDtoOnUpdate(updatedGroup);
    }

    /**
     * Deletes a group and notifies members.
     * The method:
     * 1. Verifies the user has permission to delete the group
     * 2. Deletes the group from the database
     * 3. Publishes a GroupDeletedEvent to notify members
     *
     * @param id the ID of the group to delete
     * @param username the username of the user attempting to delete
     * @throws ForbiddenActionException if the user is not the group admin
     * @throws EntityNotFoundException if the group is not found
     */
    @Transactional
    public void deleteGroup(Long id, String username) {
        var group = getGroupByIdFromDb(id);
        checkIfActionAllowed(username, group);
        groupRepository.deleteById(id);
        eventPublisher.publishGroupDeletedEvent(new GroupDeletedEvent(this, group));
    }

    /**
     * Adds a new member to a group.
     *
     * @param groupId the ID of the group
     * @param receiver the user to add to the group
     * @throws ResourceAlreadyExistsException if the user is already a member
     * @throws EntityNotFoundException if the group is not found
     */
    @Transactional
    public void addMember(Long groupId, User receiver) {
        var group = getGroupByIdFromDb(groupId);
        if (group.getMembers().contains(receiver)) {
            throw new ResourceAlreadyExistsException(ResourceType.USER, "username: " + receiver.getUsername());
        }
        receiver.getGroups().add(group);
        group.getMembers().add(receiver);
        groupRepository.save(group);
        log.info("[GroupService]: adding {} to the group", receiver.getUsername());
    }

    /**
     * Removes a user from a group.
     * The method:
     * 1. Verifies the user is not the group admin
     * 2. Checks if the user is a member of the group
     * 3. Removes the user from the group
     *
     * @param groupId the ID of the group
     * @param memberToDelete the username of the member to remove
     * @param username the username of the user performing the action
     * @return Set of UserDto objects representing the remaining group members
     * @throws ForbiddenActionException if attempting to remove the group admin
     * @throws ResourceNotFoundException if the user is not a member of the group
     */
    @Transactional
    public Set<UserDto> removeUserFromGroup(Long groupId, String memberToDelete, String username) {
        var group = getGroupByIdFromDb(groupId);
        checkIfAdmin(memberToDelete, group);
        var userToDelete = userService.getUserFromDb(memberToDelete);
        checkIfUserExistsInGroup(group, userToDelete);
        group.getMembers().remove(userToDelete);
        var updatedMemberList = groupRepository.save(group).getMembers();
        return updatedMemberList.stream()
                .map(userMapper::modelToDtoOnGroupCreate)
                .collect(Collectors.toSet());
    }

    /**
     * Handles a user leaving a group.
     * The method:
     * 1. Verifies the user is a member of the group
     * 2. Checks if the user is not the group admin
     * 3. Removes the user from the group and its events
     * 4. Publishes a GroupLeaveEvent to notify other members
     *
     * @param groupId the ID of the group
     * @param username the username of the user leaving
     * @throws ForbiddenActionException if the user is the group admin
     * @throws ResourceNotFoundException if the user is not a member of the group
     */
    @Transactional
    public void leaveGroup(Long groupId, String username) {
        Group groupToLeave = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));

        User currentUser = userService.getUserFromDb(username);

        if (!groupToLeave.getMembers().contains(currentUser)) {
            throw new ResourceNotFoundException(ResourceType.USER, "username: " + currentUser.getUsername());
        }

        checkIfAdmin(username, groupToLeave);
        eventPublisher.publishGroupLeaveEvent(new GroupLeaveEvent(this, groupToLeave, currentUser));

        groupToLeave.getMembers().remove(currentUser);
        currentUser.getGroups().remove(groupToLeave);

        List<Event> eventsToRemove = currentUser.getGoingEvents().stream()
                .filter(event -> event.getGroup().getId().equals(groupId))
                .toList();

        for (Event event : eventsToRemove) {
            event.getGoingMembers().remove(currentUser);
            currentUser.getGoingEvents().remove(event);
        }

        eventRepository.saveAll(eventsToRemove);
        groupRepository.save(groupToLeave);
    }

    /**
     * Retrieves all events associated with a group.
     *
     * @param groupId the ID of the group
     * @return List of EventDto objects containing the group's events
     * @throws EntityNotFoundException if the group is not found
     */
    public List<EventDto> getAllGroupEvents(Long groupId) {
        var group = getGroupByIdFromDb(groupId);
        return group.getEvents().stream()
                .map(eventMapper::modelToDtoOnGet).toList();
    }

    /**
     * Verifies if a user is a member of a group.
     *
     * @param group the group to check
     * @param userToDelete the user to verify
     * @throws ResourceNotFoundException if the user is not a member of the group
     */
    public void checkIfUserExistsInGroup(Group group, User userToDelete) {
        if (!group.getMembers().contains(userToDelete)) {
            throw new ResourceNotFoundException(ResourceType.USER, "username: " + userToDelete.getUsername());
        }
    }

    /**
     * Retrieves a group entity from the database by ID.
     *
     * @param id the ID of the group
     * @return the Group entity
     * @throws EntityNotFoundException if the group is not found
     */
    public Group getGroupByIdFromDb(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found by id: " + id));
    }

    /**
     * Verifies if a user has permission to perform an action on a group.
     *
     * @param username the username of the user
     * @param group the group to check
     * @throws ForbiddenActionException if the user is not the group admin
     */
    private void checkIfActionAllowed(String username, Group group) {
        if (!group.getAdmin().getUsername().equals(username)) {
            throw new ForbiddenActionException("You are not allowed to delete this group.");
        }
    }

    /**
     * Verifies if a user is not the group admin.
     *
     * @param username the username of the user
     * @param group the group to check
     * @throws ForbiddenActionException if the user is the group admin
     */
    private void checkIfAdmin(String username, Group group) {
        if (group.getAdmin().getUsername().equalsIgnoreCase(username)) {
            throw new ForbiddenActionException("You cannot leave the group, dear Admin!");
        }
    }

    /**
     * Calculates the availability of all group members.
     *
     * @param members the set of group members
     * @return Map of dates to sets of available users
     */
    private Map<LocalDate, Set<UserDto>> groupAvailableDays(Set<User> members) {
        return members.stream()
                .flatMap(member -> member.getAvailableDays().stream()
                        .map(date -> Map.entry(
                                date,
                                userMapper.modelToDtoOnGroupCreate(member)
                        )))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(
                                Map.Entry::getValue,
                                Collectors.toSet()
                        )
                ));
    }

    /**
     * Finds a group by its name.
     *
     * @param groupName the name of the group
     * @return the Group entity
     * @throws ResourceNotFoundException if the group is not found
     */
    public Group findByName(String groupName) {
        return groupRepository.findByName(groupName)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.GROUP, "name: " + groupName));
    }

    /**
     * Retrieves a group with its members by ID.
     *
     * @param id the ID of the group
     * @return the Group entity with its members
     * @throws ResourceNotFoundException if the group is not found
     */
    public Group getGroupByIdWithMembers(Long id) {
        return groupRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.GROUP, "id: " + id));
    }
}