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

    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable final Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(
            Authentication authentication, @RequestBody final GroupDto groupDto) {
        var username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(username, groupDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupDto> editGroupName(@RequestBody final GroupDto editedGroup, @PathVariable final Long id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(groupService.updateGroup(editedGroup, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(Authentication authentication, @PathVariable final Long id) {
        var username = authentication.getName();
        groupService.deleteGroup(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Set<UserDto>> addNewMember(
            @PathVariable final Long id,
            @RequestBody final UserDto userDto
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(groupService.addMember(id, userDto));
    }








}
