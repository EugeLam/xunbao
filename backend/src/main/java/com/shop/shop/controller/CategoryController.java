package com.shop.shop.controller;

import com.shop.shop.dto.ApiResponse;
import com.shop.shop.dto.CategoryDTO;
import com.shop.shop.dto.CreateCategoryRequest;
import com.shop.shop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategories(
            @RequestParam(required = false) Long parentId) {
        List<CategoryDTO> categories;
        if (parentId == null) {
            categories = categoryService.getAllCategories();
        } else {
            categories = categoryService.getSubcategories(parentId);
        }
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.createCategory(request, userId)));
    }
}
