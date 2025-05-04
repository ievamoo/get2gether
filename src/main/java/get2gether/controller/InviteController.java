package get2gether.controller;

import get2gether.dto.InviteDto;
import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    @PostMapping
    ResponseEntity<String> createInvite(Authentication authentication, @RequestBody final InviteDto inviteDto) {
        var user = authentication.getName();
        var inviteResponse = inviteService.createNewInvite(inviteDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(inviteResponse);
    }

}
