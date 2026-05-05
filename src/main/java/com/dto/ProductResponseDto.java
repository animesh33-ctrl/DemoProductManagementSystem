package com.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductResponseDto {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Boolean active;
    private CategoryResponseDto category;
    private String createdBy;  // username only
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}