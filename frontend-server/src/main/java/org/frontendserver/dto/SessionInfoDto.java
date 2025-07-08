package org.frontendserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoDto {
    private String id;
    private String userAgent;
    private String ipAddress;
    private Instant createdAt;
    private Instant expiresAt;
}