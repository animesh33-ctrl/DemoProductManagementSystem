package com.service;

import com.dto.CategoryRequestDto;
import com.dto.CategoryResponseDto;
import com.entity.CategoryEntity;
import com.exception.ResourceConflictException;
import com.exception.ResourceNotFoundException;
import com.repository.CategoryRepository;
import com.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new ResourceConflictException("Category already exists: " + dto.getName());
        }
        CategoryEntity entity = modelMapper.map(dto, CategoryEntity.class);
        return modelMapper.map(categoryRepository.save(entity), CategoryResponseDto.class);
    }

    @Override
    @Transactional
    public CategoryResponseDto updateCategory(UUID id, CategoryRequestDto dto) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        if (!category.getName().equals(dto.getName())
                && categoryRepository.existsByName(dto.getName())) {
            throw new ResourceConflictException("Category name already taken: " + dto.getName());
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return modelMapper.map(categoryRepository.save(category), CategoryResponseDto.class);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponseDto getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .map(c -> modelMapper.map(c, CategoryResponseDto.class))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(c -> modelMapper.map(c, CategoryResponseDto.class))
                .toList();
    }
}