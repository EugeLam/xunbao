package com.shop.shop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.shop.dto.*;
import com.shop.shop.service.ProductService;
import com.shop.shop.service.VariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final VariantService variantService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.searchProducts(keyword, categoryId, minPrice, maxPrice, inStock, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProduct(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(productService.createProduct(request, userId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        productService.deleteProduct(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }

    @PutMapping("/{id}/image")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(productService.uploadImage(id, file, userId)));
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<Object>> getVariants(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(variantService.getVariants(id)));
    }

    @PostMapping("/{id}/variants")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse<Object>> addVariant(
            @PathVariable Long id,
            @Valid @RequestBody CreateVariantRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(variantService.addVariant(id, request, userId)));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<Object>> getReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(variantService.getReviews(id, page, size)));
    }

    @PostMapping("/{id}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> addReview(
            @PathVariable Long id,
            @Valid @RequestBody CreateReviewRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(variantService.addReview(id, request, userId)));
    }
}
