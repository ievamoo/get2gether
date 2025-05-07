package get2gether.controller;

import get2gether.dto.InviteDto;
import get2gether.model.Invite;
import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    @PostMapping
    ResponseEntity<String> createInvite(Authentication authentication, @RequestBody final InviteDto inviteDto) {
        var username = authentication.getName();
        var inviteResponse = inviteService.createNewInvite(inviteDto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(inviteResponse);
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
