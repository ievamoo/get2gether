package get2gether.controller;

import get2gether.dto.UserDto;
import get2gether.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Controller responsible for managing user-related operations.
 * Provides REST endpoints for user profile management, including:
 * - User profile retrieval and updates
 * - Account deletion
 * - Availability management
 * - User listing and discovery
 * Handles authentication and ensures proper access control for user operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the profile information of the currently authenticated user.
     *
     * @param authentication the authentication object containing the current user's details
     * @return ResponseEntity containing the current user's profile information
     */
    @GetMapping
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        var username = authentication.getName();
        var currentUser = userService.getUserByUsername(username);
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Updates the profile information of the currently authenticated user.
     *
     * @param authentication the authentication object containing the current user's details
     * @param updatedUserDto the updated user information
     * @return ResponseEntity with status ACCEPTED containing the updated user profile
     */
    @PutMapping
    public ResponseEntity<UserDto> updateCurrentUser(Authentication authentication,
                                                     @RequestBody final UserDto updatedUserDto) {
        var username = authentication.getName();
        var updatedUser = userService.updateCurrentUser(username, updatedUserDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedUser);
    }

    /**
     * Deletes the account of the currently authenticated user.
     *
     * @param authentication the authentication object containing the current user's details
     * @return ResponseEntity with no content if deletion is successful
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        var username = authentication.getName();
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the available days for the currently authenticated user.
     *
     * @param authentication the authentication object containing the current user's details
     * @param availableDays the set of dates when the user is available
     * @return ResponseEntity containing the updated set of available days
     */
    @PutMapping("/availability")
    public ResponseEntity<Set<LocalDate>> setAvailableDays(Authentication authentication,
                                                           @RequestBody final Set<LocalDate> availableDays) {
        var userName = authentication.getName();
        var updatedDates = userService.updateAvailableDays(userName, availableDays);
        return ResponseEntity.status(HttpStatus.OK).body(updatedDates);
    }

    /**
     * Retrieves the available days for the currently authenticated user.
     *
     * @param authentication the authentication object containing the current user's details
     * @return ResponseEntity containing the set of available days
     */
    @GetMapping("/availability")
    public ResponseEntity<Set<LocalDate>> getAvailableDays(Authentication authentication) {
        var userName = authentication.getName();
        var availableDays = userService.getAvailableDays(userName);
        return ResponseEntity.status(HttpStatus.OK).body(availableDays);
    }

    /**
     * Retrieves a list of all users in the system.
     * This endpoint is typically restricted to administrators.
     *
     * @return ResponseEntity containing a list of all user profiles
     */
    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

}
