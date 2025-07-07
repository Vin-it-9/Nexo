package org.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.userservice.entity.User;
import org.userservice.entity.VerificationToken;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByUser(User user);
}