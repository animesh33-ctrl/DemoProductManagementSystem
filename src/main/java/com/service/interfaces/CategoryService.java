package com.service.interfaces;

import com.dto.CategoryRequestDto;
import com.dto.CategoryResponseDto;
import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponseDto createCategory(CategoryRequestDto dto);
    CategoryResponseDto updateCategory(UUID id, CategoryRequestDto dto);
    void deleteCategory(UUID id);
    CategoryResponseDto getCategoryById(UUID id);
    List<CategoryResponseDto> getAllCategories();
}