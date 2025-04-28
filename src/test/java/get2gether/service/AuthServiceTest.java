package get2gether.service;

import get2gether.TestData;
import get2gether.exception.RegistrationException;
import get2gether.repository.UserRepository;
import get2gether.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private  AuthenticationManager authenticationManager;

    @Mock
    private  CustomUserDetailsService userDetailsService;

    @Mock
    private  UserRepository userRepository;

    @Mock
    private  PasswordEncoder passwordEncoder;

    @Mock
    private  JwtUtil jwtUtil;

    @InjectMocks
    private AuthService testAuthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void authenticateAndGenerateToken_whenUserExistsAndCredentialsValid() {
        var user = TestData.getTestUser();
        var expectedToken = "fake-jwt-token";

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("USER")
                .build();

        when(userRepository.existsByUsername("test@gmail.com")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        when(userDetailsService.loadUserByUsername("test@gmail.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

        var actualToken = testAuthService.authenticateAndGenerateToken(user.getUsername(), user.getPassword());

        assertEquals(expectedToken, actualToken);
    }

    @Test
    void authenticateAndGenerateToken_whenUserDoesNotExist() {
        when(userRepository.existsByUsername("test@gmail.com")).thenReturn(false);

        assertThrows(UsernameNotFoundException.class,
                () -> testAuthService.authenticateAndGenerateToken("test@gmail.com", "password"));
    }

    @Test
    void authenticateAndGenerateToken_whenUserExistsAndCredentialsNotValid() {
        when(userRepository.existsByUsername("test@gmail.com")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        assertThrows(BadCredentialsException.class,
                () -> testAuthService.authenticateAndGenerateToken("test@gmail.com", "password"));
    }


    @Test
    void registerAndGenerateToken_whenUsernameExistShouldRegisterAndReturnToken() {
        var registerRequest = TestData.getRegisterRequestDto();
        var expectedToken = "fake-jwt-token";

        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");

        var savedUser = get2gether.model.User.builder()
                .username(registerRequest.getUsername())
                .password("encoded_password")
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .roles(List.of(get2gether.model.Role.USER))
                .build();

        when(userRepository.save(any(get2gether.model.User.class))).thenReturn(savedUser);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getUsername())
                .password(savedUser.getPassword())
                .authorities("USER")
                .build();

        when(userDetailsService.loadUserByUsername(registerRequest.getUsername())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

        var actualToken = testAuthService.registerAndGenerateToken(registerRequest);

        assertEquals(expectedToken, actualToken);
    }

    @Test
    void registerAndGenerateToken_shouldThrowExceptionWhenUsernameAlreadyExists() {
        var registerRequest = TestData.getRegisterRequestDto();

        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertThrows(RegistrationException.class, () ->
                testAuthService.registerAndGenerateToken(registerRequest)
        );
    }
}