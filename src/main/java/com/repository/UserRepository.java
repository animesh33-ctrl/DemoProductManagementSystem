package com.repository;

import com.entity.UserEntity;
import com.repository.annotations.ReadFast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    @ReadFast
    Optional<UserEntity> findByUsername(String username,String email);

    @ReadFast
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);
}
