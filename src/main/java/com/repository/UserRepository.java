package com.repository;

import com.entity.UserEntity;
import com.repository.annotations.ReadFast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    @ReadFast
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username OR u.email = :email")
    Optional<UserEntity> findByUsername(String username,String email);

    @ReadFast
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);
}
