package get2gether.controller;

import get2gether.dto.AuthRequestDto;
import get2gether.dto.AuthResponseDto;
import get2gether.dto.RegisterRequestDto;
import get2gether.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for handling user authentication and registration.
 * Provides endpoints for user login and registration, generating JWT tokens upon successful authentication.
 * Manages the authentication lifecycle including:
 * - User login with JWT token generation
 * - New user registration with account creation
 * - Secure password handling and validation
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user and generates a JWT token upon successful login.
     * Validates the provided username and password credentials.
     *
     * @param authRequest the authentication request containing username and password
     * @return ResponseEntity containing the JWT token if authentication is successful
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> createAuthenticationToken(@RequestBody AuthRequestDto authRequest) {
        String jwt = authService.authenticateAndGenerateToken(
                authRequest.getUsername(), authRequest.getPassword());
        return ResponseEntity.ok(new AuthResponseDto(jwt));
    }

    /**
     * Registers a new user and generates a JWT token upon successful registration.
     * Creates a new user account with the provided registration details.
     *
     * @param request the registration request containing user details
     * @return ResponseEntity with status CREATED containing the JWT token if registration is successful
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerUser(@RequestBody RegisterRequestDto request) {
        String jwt = authService.registerAndGenerateToken(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDto(jwt));
    }

}
