package com.repository;

import com.entity.UserEntity;
import com.repository.annotations.ReadFast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    @ReadFast
    Optional<UserEntity> findByUsername(String username);

    @ReadFast
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.failedAttempts = u.failedAttempts + 1 WHERE u.username = :username")
    void incrementFailedAttempts(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.failedAttempts = 0, u.lockedUntil = null WHERE u.username = :username")
    void resetFailedAttempts(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.lockedUntil = :lockedUntil WHERE u.username = :username")
    void lockAccount(@Param("username") String username, @Param("lockedUntil") LocalDateTime lockedUntil);
}
