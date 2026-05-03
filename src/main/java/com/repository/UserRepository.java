package com.repository;

import com.entity.UserEntity;
import com.repository.annotations.ReadFast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    @ReadFast
    Optional<UserEntity> findByUsername(String username);

    @ReadFast
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);
}
