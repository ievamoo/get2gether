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

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable final Long groupId) {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    @GetMapping("/{groupId}/events")
    public ResponseEntity<List<EventDto>> getAllGroupEvents(@PathVariable final Long groupId) {
        return ResponseEntity.ok(groupService.getAllGroupEvents(groupId));
    }

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(
            Authentication authentication, @RequestBody final GroupDto groupDto) {
        var username = authentication.getName();
        var group = groupService.createGroup(username, groupDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
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


//    @PostMapping("/{groupId}/members")
//    public ResponseEntity<Set<GroupDto>> joinGroup(
//            @PathVariable final Long groupId,
//            Authentication authentication
//    ) {
//        var username = authentication.getName();
//        var updatedGroups = groupService.addMember(groupId, username);
//        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedGroups);
//    }

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
