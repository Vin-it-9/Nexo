package org.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.userservice.entity.RefreshToken;
import org.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUserAndRevokedFalse(User user);


    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = ?1 AND r.revoked = false")
    void revokeAllUserTokens(User user);

    @Modifying
    void deleteByUser(User user);

    Optional<RefreshToken> findByUserAndUserAgentAndIpAddressAndRevokedFalse(User user, String userAgent, String ipAddress);

}