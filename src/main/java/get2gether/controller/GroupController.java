package get2gether.controller;

import get2gether.dto.GroupDto;
import get2gether.dto.GroupRequestDtoNotNeeded;
import get2gether.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
            Authentication authentication,  @RequestBody final GroupDto groupDto) {
        var username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(username, groupDto));
    }
}
