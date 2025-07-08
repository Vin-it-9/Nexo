package org.frontendserver.config;

import org.frontendserver.dto.SessionInfoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;
    private final String userServiceValidationUrl;
    private final String userServiceLoginUrl;
    private final String userServiceRegisterUrl;
    private final String userServiceRefreshTokenUrl;

    public UserServiceClient(
            RestTemplate restTemplate,
            @Value("${services.user-service.url}") String userServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
        this.userServiceValidationUrl = userServiceUrl + "/api/internal/validate-token";
        this.userServiceLoginUrl = userServiceUrl + "/api/auth/login";
        this.userServiceRegisterUrl = userServiceUrl + "/api/auth/register";
        this.userServiceRefreshTokenUrl = userServiceUrl + "/api/auth/refresh-token";
    }

    public Map<String, Object> validateToken(String authHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    userServiceValidationUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            return Map.of("valid", false, "error", e.getMessage());
        }
    }

    public ResponseEntity<Map> login(Map<String, String> loginRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

            return restTemplate.exchange(
                    userServiceLoginUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Map> register(Map<String, String> registerRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(registerRequest, headers);

            return restTemplate.exchange(
                    userServiceRegisterUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Map> refreshToken(String refreshToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("refreshToken", refreshToken != null ? refreshToken : "");

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            return restTemplate.exchange(
                    userServiceRefreshTokenUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Token refresh failed: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Map> getUserProfile(String authHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", cleanAuthHeader(authHeader));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                    userServiceUrl + "/api/auth/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user profile: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Void> terminateSession(String authHeader, String sessionId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", cleanAuthHeader(authHeader));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                    userServiceUrl + "/api/auth/sessions/" + sessionId,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to terminate session: " + e.getMessage(), e);
        }
    }

    private String cleanAuthHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return "Bearer ";
        }
        String token = authHeader.replaceAll("(?i)Bearer\\s+", "").trim();
        if (token.length() > 0 && !token.startsWith("ey")) {
            if (token.startsWith("y") && token.length() > 10) {
                token = "e" + token;
            }
        }
        return "Bearer " + token;
    }

    public ResponseEntity<List<SessionInfoDto>> getSessions(String authHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", cleanAuthHeader(authHeader));

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(
                    userServiceUrl + "/api/auth/sessions",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<SessionInfoDto>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get sessions: " + e.getMessage(), e);
        }
    }
}