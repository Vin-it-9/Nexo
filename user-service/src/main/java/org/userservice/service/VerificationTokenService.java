package org.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.userservice.entity.User;
import org.userservice.entity.VerificationToken;
import org.userservice.exception.TokenException;
import org.userservice.repository.UserRepository;
import org.userservice.repository.VerificationTokenRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    @Value("${application.security.verification-token.expiration}")
    private long verificationTokenDurationMs;

    @Transactional
    public VerificationToken createVerificationToken(User user) {

        verificationTokenRepository.deleteByUser(user);

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(verificationTokenDurationMs))
                .used(false)
                .build();

        return verificationTokenRepository.save(verificationToken);
    }

    @Transactional
    public void sendVerificationEmail(User user) {
        VerificationToken token = createVerificationToken(user);
        System.out.println("Sending verification email to: " + user.getEmail());
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new TokenException("User email is not set");
        }
        System.out.println("Verification token: " + token.getToken());
        emailService.sendVerificationEmail(user.getEmail(), token.getToken());
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new TokenException("Verification token has expired");
        }

        if (verificationToken.isUsed()) {
            throw new TokenException("Verification token has already been used");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

    @Transactional
    public void resendVerificationToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new TokenException("User not found"));

        if (user.isEmailVerified()) {
            throw new TokenException("Email is already verified");
        }

        sendVerificationEmail(user);
    }
}