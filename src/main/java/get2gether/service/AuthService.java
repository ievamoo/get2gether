package get2gether.service;

import get2gether.dto.RegisterRequestDto;
import get2gether.exception.RegistrationException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.model.ResourceType;
import get2gether.model.Role;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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

    @Transactional
    public String registerAndGenerateToken(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RegistrationException("Username already exists");
        }
        var user = createUser(request);
        userRepository.save(user);
        return jwtUtil.generateToken(buildUserDetails(user));

    }

    private User createUser(RegisterRequestDto request) {
        return User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(List.of(Role.USER))
                .build();
    }

//    private String generateToken(String username) {
//        var userDetails = userDetailsService.loadUserByUsername(username);
//        return jwtUtil.generateToken(userDetails);
//    }

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
