package get2gether.controller;

import get2gether.dto.InviteDto;
import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for managing group and event invitations.
 * Provides REST endpoints for invitation lifecycle management, including:
 * - Creating new group invitations
 * - Processing invitation responses (accept/decline)
 * - Invitation validation and authorization
 * Handles authentication and ensures proper access control for invitation operations.
 */
@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    /**
     * Creates a new group invitation for an existing group.
     * The authenticated user must have permission to invite others to the group.
     *
     * @param authentication the authentication object containing the current user's details
     * @param inviteDto the invitation details including group and recipient information
     * @return ResponseEntity with status CREATED containing the invitation response message
     */
    @PostMapping
    ResponseEntity<String> createGroupInvite(Authentication authentication, @RequestBody final InviteDto inviteDto) {
        var username = authentication.getName();
        var inviteResponse = inviteService.createNewInviteWhenGroupAlreadyExists(inviteDto, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_PLAIN)
                .body(inviteResponse);
    }

    /**
     * Handles a user's response to a group invitation (accept or decline).
     * Updates the invitation status based on the user's response.
     *
     * @param authentication the authentication object containing the current user's details
     * @param inviteId the unique identifier of the invitation to respond to
     * @param inviteDto the response details containing the acceptance status
     * @return ResponseEntity with status OK if the response is processed successfully
     */
    @PatchMapping("/{inviteId}")
    public ResponseEntity<Void> respondToInvite(Authentication authentication,
                                                @PathVariable Long inviteId,
                                                @RequestBody InviteDto inviteDto) {
        var username = authentication.getName();
        inviteService.handleInviteResponse(username, inviteId, inviteDto.getAccepted());
        return ResponseEntity.ok().build();
    }

}
