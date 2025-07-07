package org.userservice.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.userservice.dto.*;
import org.userservice.entity.RefreshToken;
import org.userservice.entity.User;
import org.userservice.entity.enums.Role;
import org.userservice.exception.UserRegistrationException;
import org.userservice.repository.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION_MS = 15 * 60 * 1000;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserRegistrationException("Email is already in use");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .emailVerified(false)
                .accountLocked(false)
                .failedAttempts(0)
                .build();

        User savedUser = userRepository.save(user);

        verificationTokenService.sendVerificationEmail(savedUser);

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.isAccountLocked()) {
            if (unlockWhenTimeExpired(user)) {
            } else {
                throw new LockedException("Your account has been locked due to too many failed login attempts");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            if (user.getFailedAttempts() > 0) {
                loginAttemptService.resetFailedAttempts(user.getEmail());
            }

            String jwtToken = jwtService.generateToken(user);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, httpRequest);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken.getToken())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .emailVerified(user.isEmailVerified())
                    .build();
        } catch (BadCredentialsException e) {
            loginAttemptService.incrementFailedAttempts(user);
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                loginAttemptService.lockAccount(user);
                throw new LockedException("Your account has been locked due to too many failed login attempts");
            }

            throw e;
        }
    }

    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        return refreshTokenService.refreshAccessToken(request.getRefreshToken());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
    }

    @Transactional
    public void verifyEmail(String token) {
        verificationTokenService.verifyEmail(token);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        verificationTokenService.resendVerificationToken(email);
    }

    private boolean unlockWhenTimeExpired(User user) {
        if (user.getLockTime() != null && user.getLockTime().plusMillis(LOCK_TIME_DURATION_MS).isBefore(Instant.now())) {
            user.setAccountLocked(false);
            user.setLockTime(null);
            user.setFailedAttempts(0);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}