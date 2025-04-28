package get2gether.controller;

import get2gether.dto.AuthRequestDto;
import get2gether.dto.AuthResponseDto;
import get2gether.dto.RegisterRequestDto;
import get2gether.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> createAuthenticationToken(@RequestBody AuthRequestDto authRequest) {
        String jwt = authService.authenticateAndGenerateToken(
                authRequest.getUsername(), authRequest.getPassword());
        return ResponseEntity.ok(new AuthResponseDto(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerUser(@RequestBody RegisterRequestDto request) {
        String jwt = authService.registerAndGenerateToken(request);
        return ResponseEntity.ok(new AuthResponseDto(jwt));
    }

}
