package com.shop.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.shop.dto.CategoryDTO;
import com.shop.shop.dto.CreateCategoryRequest;
import com.shop.shop.mapper.CategoryMapper;
import com.shop.shop.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<CategoryDTO> getAllCategories() {
        return categoryMapper.selectList(
                        new LambdaQueryWrapper<Category>()
                                .isNull(Category::getParentId)
                                .orderByAsc(Category::getSortOrder))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getSubcategories(Long parentId) {
        return categoryMapper.selectList(
                        new LambdaQueryWrapper<Category>()
                                .eq(Category::getParentId, parentId)
                                .orderByAsc(Category::getSortOrder))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO createCategory(CreateCategoryRequest request, Long userId) {
        Category category = Category.builder()
                .name(request.getName())
                .parentId(request.getParentId())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        categoryMapper.insert(category);
        return toDTO(category);
    }

    private CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParentId())
                .sortOrder(category.getSortOrder())
                .build();
    }
}
