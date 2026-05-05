package com.security.lockoutservice;

import com.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountLockoutService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;

    public void handleFailedAttempt(String username) {
        userRepository.incrementFailedAttempts(username);

        // Fetch fresh count
        userRepository.findByUsernameOrEmail(username, username).ifPresent(user -> {
            if (user.getFailedAttempts() >= MAX_ATTEMPTS) {
                LocalDateTime lockedUntil = LocalDateTime.now()
                        .plusMinutes(LOCK_DURATION_MINUTES);
                userRepository.lockAccount(username, lockedUntil);
                log.warn("Account locked: {} until {}", username, lockedUntil);
            }
        });
    }

    public void handleSuccessfulLogin(String username) {
        userRepository.resetFailedAttempts(username);
    }
}