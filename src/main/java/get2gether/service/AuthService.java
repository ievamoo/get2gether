package get2gether.service;

import get2gether.dto.RegisterRequestDto;
import get2gether.exception.RegistrationException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.enums.ResourceType;
import get2gether.enums.Role;
import get2gether.model.User;
import get2gether.repository.UserRepository;
import get2gether.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for handling user authentication and registration.
 * Provides functionality for user login, registration, and JWT token generation.
 * Manages user authentication state and ensures secure password handling.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Authenticates a user and generates a JWT token upon successful authentication.
     * The method:
     * 1. Verifies the user exists in the database
     * 2. Authenticates the user's credentials
     * 3. Generates a JWT token for the authenticated user
     *
     * @param username the username to authenticate
     * @param password the password to verify
     * @return JWT token for the authenticated user
     * @throws ResourceNotFoundException if the user is not found
     */
    public String authenticateAndGenerateToken(String username, String password) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException(ResourceType.USER, "username: " + username);
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtUtil.generateToken(userDetails);
    }

    /**
     * Registers a new user and generates a JWT token.
     * The method:
     * 1. Checks if the username is already taken
     * 2. Creates a new user with encoded password
     * 3. Saves the user to the database
     * 4. Generates a JWT token for the new user
     *
     * @param request the registration request containing user details
     * @return JWT token for the newly registered user
     * @throws RegistrationException if the username already exists
     */
    @Transactional
    public String registerAndGenerateToken(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RegistrationException("Username already exists");
        }
        var user = createUser(request);
        userRepository.save(user);
        return jwtUtil.generateToken(buildUserDetails(user));
    }

    /**
     * Creates a new User entity from registration request data.
     * Sets default role as USER and encodes the password.
     *
     * @param request the registration request containing user details
     * @return a new User entity
     */
    private User createUser(RegisterRequestDto request) {
        return User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(List.of(Role.USER))
                .build();
    }

    /**
     * Builds a Spring Security UserDetails object from a User entity.
     * Maps the user's roles to Spring Security authorities.
     *
     * @param user the user entity to convert
     * @return UserDetails object containing the user's security information
     */
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .toList())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
