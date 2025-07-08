package org.frontendserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.frontendserver.config.UserServiceClient;
import org.frontendserver.dto.LoginRequest;
import org.frontendserver.dto.RefreshTokenRequest;
import org.frontendserver.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs for user login, registration, and session management")
public class AuthApiController {

    @Autowired
    private UserServiceClient userServiceClient;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates a user and returns JWT tokens")
    public ResponseEntity<Map> login(@RequestBody LoginRequest request) {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", request.getEmail());
        loginRequest.put("password", request.getPassword());
        return userServiceClient.login(loginRequest);
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Registers a new user")
    public ResponseEntity<Map> register(@RequestBody RegisterRequest request) {
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("fullName", request.getFullName());
        registerRequest.put("email", request.getEmail());
        registerRequest.put("password", request.getPassword());
        return userServiceClient.register(registerRequest);
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh token",
            description = "Refreshes JWT token using refresh token. " +
                    "Example request: {\"refreshToken\": \"your-refresh-token-here\"}"
    )
    public ResponseEntity<Map> refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        return userServiceClient.refreshToken(request.getRefreshToken());
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get user profile",
            description = "Returns the profile of the authenticated user. " +
                    "Requires Bearer token in Authorization header",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User profile retrieved successfully"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Missing or invalid Authorization header",
                            content = @Content(schema = @Schema(implementation = Object.class)))
            }
    )
    public ResponseEntity<Map> getUserProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Unauthorized",
                            "message", "Authorization header is required",
                            "status", 401
                    ));
        }
        return userServiceClient.getUserProfile(authHeader);
    }

    @GetMapping("/sessions")
    @Operation(
            summary = "Get active sessions",
            description = "Returns all active sessions for the authenticated user. " +
                    "Requires Bearer token in Authorization header",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Sessions retrieved successfully"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Missing or invalid Authorization header",
                            content = @Content(schema = @Schema(implementation = Object.class)))
            }
    )
    public ResponseEntity<?> getSessions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Unauthorized",
                            "message", "Authorization header is required",
                            "status", 401
                    ));
        }
        try {
            return userServiceClient.getSessions(authHeader);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Internal Server Error",
                            "message", e.getMessage(),
                            "status", 500
                    ));
        }
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(
            summary = "Terminate session",
            description = "Terminates a specific user session. " +
                    "Requires Bearer token in Authorization header",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Session terminated successfully"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Missing or invalid Authorization header",
                            content = @Content(schema = @Schema(implementation = Object.class)))
            }
    )
    public ResponseEntity<Object> terminateSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String sessionId) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Unauthorized",
                            "message", "Authorization header is required",
                            "status", 401
                    ));
        }
        userServiceClient.terminateSession(authHeader, sessionId);
        return ResponseEntity.noContent().build();
    }
}