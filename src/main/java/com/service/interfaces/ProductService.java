package com.service.interfaces;

import com.dto.ProductRequestDto;
import com.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto dto, UserDetails currentUser);
    ProductResponseDto updateProduct(UUID id, ProductRequestDto dto);
    void deleteProduct(UUID id);
    void restoreProduct(UUID id);
    ProductResponseDto getProductById(UUID id);
    Page<ProductResponseDto> getAllProducts(Pageable pageable);
    Page<ProductResponseDto> searchProducts(String search, UUID categoryId,
                                            BigDecimal minPrice, BigDecimal maxPrice,
                                            Pageable pageable);
}