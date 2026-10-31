package edu.uam.futboldb.user;

import edu.uam.futboldb.user.UserDtos.LoginRequest;
import edu.uam.futboldb.user.UserDtos.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return new TokenResponse(authService.login(request.email(), request.password()));
    }
}
