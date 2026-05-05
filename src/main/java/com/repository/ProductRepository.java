package com.repository;

import com.entity.ProductEntity;
import com.repository.annotations.ReadFast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    @ReadFast
    Page<ProductEntity> findAllByActiveTrue(Pageable pageable);

    @ReadFast
    Page<ProductEntity> findAllByCategoryIdAndActiveTrue(UUID categoryId, Pageable pageable);

    @ReadFast
    @Query("""
        SELECT p FROM ProductEntity p
        WHERE p.active = true
        AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    """)
    Page<ProductEntity> searchProducts(
            @Param("search") String search,
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @ReadFast
    Optional<ProductEntity> findByIdAndActiveTrue(UUID id);

    boolean existsByNameAndCategoryId(String name, UUID categoryId);
}