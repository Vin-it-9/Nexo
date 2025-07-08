package org.frontendserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    @Schema(description = "Refresh token", example = "65e3ede5-2589-4563-9093-acb242863fb7")
    private String refreshToken;
}