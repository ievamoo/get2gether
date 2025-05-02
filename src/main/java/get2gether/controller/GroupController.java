package get2gether.controller;

import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable final Long groupId) {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(
            Authentication authentication, @RequestBody final GroupDto groupDto) {
        var username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(username, groupDto));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupDto> editGroupName(@RequestBody final GroupDto editedGroup, @PathVariable final Long groupId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(groupService.updateGroup(editedGroup, groupId));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(Authentication authentication, @PathVariable final Long groupId) {
        var username = authentication.getName();
        groupService.deleteGroup(groupId, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Set<UserDto>> addNewMember(
            @PathVariable final Long groupId,
            @RequestBody final UserDto userDto
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(groupService.addMember(groupId, userDto));
    }

    @DeleteMapping("/{groupId}/members/{memberToDelete}")
    public ResponseEntity<Set<UserDto>> removeUserFromGroup(
            @PathVariable final Long groupId,
            @PathVariable final String  memberToDelete,
            Authentication authentication
    ) {
        var username = authentication.getName();
        var updatedMembers = groupService.removeUserFromGroup(groupId, memberToDelete, username);
        return ResponseEntity.accepted().body(updatedMembers);
    }

    @DeleteMapping("/{groupId}/members/")
    public ResponseEntity<Set<GroupDto>> leaveGroup(
            @PathVariable final Long groupId,
            Authentication authentication
    ) {
        var username = authentication.getName();
        var updatedGroups = groupService.leaveGroup(groupId, username);
        return ResponseEntity.accepted().body(updatedGroups);
    }

}
