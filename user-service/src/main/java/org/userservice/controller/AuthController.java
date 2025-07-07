package org.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.userservice.dto.*;
import org.userservice.entity.RefreshToken;
import org.userservice.entity.User;
import org.userservice.service.AuthenticationService;
import org.userservice.service.RefreshTokenService;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(request, httpRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(
            @RequestParam String email) {
        authenticationService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionInfoDto>> getActiveSessions(
            @AuthenticationPrincipal User user) {
        List<RefreshToken> tokens = refreshTokenService.findAllActiveTokensByUser(user);

        List<SessionInfoDto> sessions = tokens.stream()
                .map(token -> SessionInfoDto.builder()
                        .id(token.getToken())
                        .userAgent(token.getUserAgent())
                        .ipAddress(token.getIpAddress())
                        .createdAt(token.getExpiryDate().minusMillis(refreshTokenService.getRefreshTokenDuration()))
                        .expiresAt(token.getExpiryDate())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> terminateSession(
            @AuthenticationPrincipal User user,
            @PathVariable String sessionId) {
        refreshTokenService.terminateSession(sessionId, user);
        return ResponseEntity.ok().build();
    }
}