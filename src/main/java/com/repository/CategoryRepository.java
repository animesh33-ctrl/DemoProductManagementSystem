package com.repository;

import com.entity.CategoryEntity;
import com.repository.annotations.ReadFast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    @ReadFast
    Optional<CategoryEntity> findByName(String name);

    @ReadFast
    boolean existsByName(String name);
}