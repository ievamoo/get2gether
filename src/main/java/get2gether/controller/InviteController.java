package get2gether.controller;

import get2gether.dto.InviteDto;
import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    @PostMapping
    ResponseEntity<String> createGroupInvite(Authentication authentication, @RequestBody final InviteDto inviteDto) {
        var username = authentication.getName();
        var inviteResponse = inviteService.createNewInviteWhenGroupAlreadyExists(inviteDto, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_PLAIN)
                .body(inviteResponse);
    }

    @PatchMapping("/{inviteId}")
    public ResponseEntity<Void> respondToInvite(
            Authentication authentication,
            @PathVariable Long inviteId,
            @RequestBody InviteDto inviteDto
    ) {
        var username = authentication.getName();
        inviteService.handleInviteResponse(username, inviteId, inviteDto.getAccepted());
        return ResponseEntity.ok().build();
    }

}
