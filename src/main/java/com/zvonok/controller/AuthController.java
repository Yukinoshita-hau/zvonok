package com.zvonok.controller;

import com.zvonok.security.JwtTokenProvider;
import com.zvonok.security.dto.UserPrincipal;
import com.zvonok.service.AuthService;
import com.zvonok.service.dto.AuthResponse;
import com.zvonok.service.dto.request.LoginRequest;
import com.zvonok.service.dto.request.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - username: {}", request.getUsername());

        return authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getDisplayName()
        );
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - username: {}", request.getUsername());

        return authService.login(
                request.getUsername(),
                request.getPassword()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        log.warn("UserPrincipal - {}", principal.getName());

        String username = principal.getName();

        Map<String, Object> response = Map.of(
                "username", username,
                "message", "Ты успешно аутефицировался!",
                "timestamp", System.currentTimeMillis()
        );

        return ResponseEntity.ok(response);
    }
}
