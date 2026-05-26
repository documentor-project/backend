package com.documentor.backend.presentation.auth;

import com.documentor.backend.service.auth.AuthService;
import com.documentor.backend.service.auth.TokenResult;
import com.documentor.backend.service.auth.UserResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signUp(@Valid @RequestBody SignUpRequest request) {
        return UserResponse.from(authService.signUp(request.email(), request.password(), request.nickname()));
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        TokenResult result = authService.login(request.email(), request.password());
        return TokenResponse.from(result);
    }

    @PostMapping("/auth/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return TokenResponse.from(authService.refresh(request.refreshToken()));
    }

    @GetMapping("/users/me")
    public UserResponse getMe(@RequestHeader("Authorization") String authorizationHeader) {
        return UserResponse.from(authService.getMe(authorizationHeader));
    }

    @PatchMapping("/users/me")
    public UserResponse updateMe(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return UserResponse.from(authService.updateMe(authorizationHeader, request.nickname()));
    }
}
