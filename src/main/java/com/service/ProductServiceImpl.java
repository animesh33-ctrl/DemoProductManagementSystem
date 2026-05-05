package com.service;

import com.dto.ProductRequestDto;
import com.dto.ProductResponseDto;
import com.entity.CategoryEntity;
import com.entity.ProductEntity;
import com.entity.UserEntity;
import com.exception.ResourceConflictException;
import com.exception.ResourceNotFoundException;
import com.repository.CategoryRepository;
import com.repository.ProductRepository;
import com.repository.UserRepository;
import com.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto dto, UserDetails currentUser) {
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (productRepository.existsByNameAndCategoryId(dto.getName(), dto.getCategoryId())) {
            throw new ResourceConflictException("Product already exists in this category");
        }

        UserEntity user = userRepository.findByUsernameOrEmail(
                        currentUser.getUsername(), currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProductEntity product = modelMapper.map(dto, ProductEntity.class);
        product.setCategory(category);
        product.setCreatedBy(user);
        product.setActive(true);

        return mapToDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(UUID id, ProductRequestDto dto) {
        ProductEntity product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Check name conflict only if name changed
        if (!product.getName().equals(dto.getName()) &&
                productRepository.existsByNameAndCategoryId(dto.getName(), dto.getCategoryId())) {
            throw new ResourceConflictException("Product name already exists in this category");
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);

        return mapToDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        ProductEntity product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setActive(false); // soft delete
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void restoreProduct(UUID id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setActive(true);
        productRepository.save(product);
    }

    @Override
    public ProductResponseDto getProductById(UUID id) {
        return productRepository.findByIdAndActiveTrue(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Override
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productRepository.findAllByActiveTrue(pageable)
                .map(this::mapToDto);
    }

    @Override
    public Page<ProductResponseDto> searchProducts(String search, UUID categoryId,
                                                   BigDecimal minPrice, BigDecimal maxPrice,
                                                   Pageable pageable) {
        return productRepository.searchProducts(search, categoryId, minPrice, maxPrice, pageable)
                .map(this::mapToDto);
    }

    private ProductResponseDto mapToDto(ProductEntity product) {
        ProductResponseDto dto = modelMapper.map(product, ProductResponseDto.class);
        dto.setCreatedBy(product.getCreatedBy().getUsername());
        return dto;
    }
}