package org.frontendserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Protected endpoints that require authentication")
@SecurityRequirement(name = "bearerAuth")
public class ProtectedApiController {

    @GetMapping("/user-info")
    @Operation(summary = "Get authenticated user info", description = "Returns information about the authenticated user")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Admin endpoint", description = "Protected endpoint for administrators only")
    public ResponseEntity<Map<String, Object>> adminEndpoint(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(Map.of(
                "message", "Hello Admin!",
                "userId", userId,
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}