package org.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.userservice.entity.User;
import org.userservice.repository.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void incrementFailedAttempts(User user) {
        int newFailAttempts = user.getFailedAttempts() + 1;
        userRepository.updateFailedAttempts(newFailAttempts, user.getEmail());
    }

    @Transactional
    public void resetFailedAttempts(String email) {
        userRepository.updateFailedAttempts(0, email);
    }

    @Transactional
    public void lockAccount(User user) {
        user.setAccountLocked(true);
        user.setLockTime(Instant.now());
        userRepository.save(user);
    }
}