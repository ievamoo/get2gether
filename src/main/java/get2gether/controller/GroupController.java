package get2gether.controller;

import get2gether.dto.EventDto;
import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Controller responsible for managing group-related operations.
 * Provides REST endpoints for group lifecycle management, including:
 * - Group creation and configuration
 * - Group updates and modifications
 * - Group deletion and cleanup
 * - Member management (adding, removing, leaving)
 * - Event coordination within groups
 * Handles authentication and authorization for group operations.
 */
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * Retrieves a group by its unique identifier.
     *
     * @param groupId the unique identifier of the group
     * @return ResponseEntity containing the group details if found
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable final Long groupId) {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    /**
     * Retrieves all events associated with a specific group.
     *
     * @param groupId the unique identifier of the group
     * @return ResponseEntity containing a list of events in the group
     */
    @GetMapping("/{groupId}/events")
    public ResponseEntity<List<EventDto>> getAllGroupEvents(@PathVariable final Long groupId) {
        return ResponseEntity.ok(groupService.getAllGroupEvents(groupId));
    }

    /**
     * Creates a new group with the authenticated user as the creator.
     *
     * @param authentication the authentication object containing the current user's details
     * @param groupDto       the group details to create
     * @return ResponseEntity with status CREATED containing the created group details
     */
    @PostMapping
    public ResponseEntity<GroupDto> createGroup(
            Authentication authentication, @RequestBody final GroupDto groupDto) {
        var username = authentication.getName();
        var group = groupService.createGroup(username, groupDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    /**
     * Updates the name of an existing group.
     *
     * @param editedGroup the updated group details
     * @param groupId     the unique identifier of the group to update
     * @return ResponseEntity with status ACCEPTED containing the updated group details
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<GroupDto> editGroup(
            @RequestBody final GroupDto editedGroup,
            @PathVariable final Long groupId,
            Authentication authentication
    ) {
        var username = authentication.getName();
        var updatedGroup = groupService.updateGroup(editedGroup, groupId, username);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedGroup);
    }

    /**
     * Deletes a group if the authenticated user has permission.
     *
     * @param authentication the authentication object containing the current user's details
     * @param groupId        the unique identifier of the group to delete
     * @return ResponseEntity with no content if deletion is successful
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(Authentication authentication,
                                            @PathVariable final Long groupId) {
        var username = authentication.getName();
        groupService.deleteGroup(groupId, username);
        return ResponseEntity.noContent().build();
    }

    /**
     * Removes a specific member from the group if the authenticated user has permission.
     *
     * @param groupId        the unique identifier of the group
     * @param memberToDelete the username of the member to remove
     * @param authentication the authentication object containing the current user's details
     * @return ResponseEntity with status ACCEPTED containing the updated list of group members
     */
    @DeleteMapping("/{groupId}/members/{memberToDelete}")
    public ResponseEntity<Set<UserDto>> removeUserFromGroup(@PathVariable final Long groupId,
                                                            @PathVariable final String memberToDelete,
                                                            Authentication authentication) {
        var username = authentication.getName();
        var updatedMembers = groupService.removeUserFromGroup(groupId, memberToDelete, username);
        return ResponseEntity.accepted().body(updatedMembers);
    }

    /**
     * Allows the authenticated user to leave a group.
     *
     * @param groupId        the unique identifier of the group to leave
     * @param authentication the authentication object containing the current user's details
     * @return ResponseEntity with no content if leaving the group is successful
     */
    @DeleteMapping("/{groupId}/members")
    public ResponseEntity<Void> leaveGroup(@PathVariable final Long groupId,
                                           Authentication authentication) {
        var username = authentication.getName();
        groupService.leaveGroup(groupId, username);
        return ResponseEntity.noContent().build();
    }

}
