package org.frontendserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public APIs", description = "Public endpoints that don't require authentication")
public class PublicApiController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Checks if the service is running")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", java.time.Instant.now().toString(),
                "service", "frontend-service"
        ));
    }

    @GetMapping("/info")
    @Operation(summary = "Service info", description = "Returns information about the frontend service")
    public ResponseEntity<Map<String, Object>> serviceInfo() {
        return ResponseEntity.ok(Map.of(
                "name", "Frontend Service",
                "version", "1.0.0",
                "description", "Frontend service for Nexo application",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}