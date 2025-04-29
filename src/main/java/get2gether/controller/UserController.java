package get2gether.controller;

import get2gether.dto.UserDto;
import get2gether.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        var username = authentication.getName();
        var currentUser = userService.getUserByUsername(username);
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping
    public ResponseEntity<UserDto> updateCurrentUser(Authentication authentication, @RequestBody final UserDto updatedUserDto) {
        var username = authentication.getName();
        var updatedUser = userService.updateCurrentUser(username, updatedUserDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedUser);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        var username = authentication.getName();
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

}
