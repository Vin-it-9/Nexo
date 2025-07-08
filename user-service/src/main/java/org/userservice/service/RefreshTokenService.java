package org.userservice.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.userservice.dto.TokenRefreshResponse;
import org.userservice.entity.RefreshToken;
import org.userservice.entity.User;
import org.userservice.exception.TokenRefreshException;
import org.userservice.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private JwtService jwtService;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenDurationMs;


    @Transactional
    public RefreshToken createRefreshToken(User user, HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIp(request);

        String deviceId = generateDeviceId(request);

        Optional<RefreshToken> existingToken = refreshTokenRepository
                .findByUserAndUserAgentAndIpAddressAndRevokedFalse(user, userAgent, ipAddress);

        if (existingToken.isPresent()) {
            RefreshToken token = existingToken.get();
            if (token.getExpiryDate().minusMillis(refreshTokenDurationMs / 2).isBefore(Instant.now())) {
                token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
                return refreshTokenRepository.save(token);
            }
            return token;
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private String generateDeviceId(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIp(request);
        return DigestUtils.md5DigestAsHex((userAgent + ipAddress).getBytes());
    }

    @Transactional
    public TokenRefreshResponse refreshAccessToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    return new TokenRefreshResponse(token, refreshToken);
                })
                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired() || token.isRevoked()) {
            refreshTokenRepository.delete(token);
        }

        return token;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    public List<RefreshToken> findAllActiveTokensByUser(User user) {
        return refreshTokenRepository.findAllByUserAndRevokedFalse(user);
    }

    @Transactional
    public void terminateSession(String tokenId, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        RefreshToken token = refreshTokenRepository.findByToken(tokenId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!token.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to terminate this session");
        }
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public long getRefreshTokenDuration() {
        return refreshTokenDurationMs;
    }
}