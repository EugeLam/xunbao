package com.shop.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.shop.dto.*;
import com.shop.shop.exception.BusinessException;
import com.shop.shop.exception.ErrorCode;
import com.shop.shop.mapper.*;
import com.shop.shop.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;
    private final CategoryMapper categoryMapper;
    private final ProductVariantMapper variantMapper;
    private final ReviewMapper reviewMapper;
    private final OssService ossService;

    public Page<ProductDTO> searchProducts(String keyword, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock, int page, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Product::getName, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }
        if (Boolean.TRUE.equals(inStock)) {
            wrapper.gt(Product::getStock, 0);
        }
        wrapper.orderByDesc(Product::getCreatedAt);

        IPage<Product> productPage = productMapper.selectPage(Page.of(page, size), wrapper);
        List<ProductDTO> dtoList = productPage.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        Page<ProductDTO> resultPage = new Page<>(page, size);
        resultPage.setRecords(dtoList);
        resultPage.setTotal(productPage.getTotal());
        return resultPage;
    }

    public ProductDTO getProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return toDTO(product);
    }

    @Transactional
    public ProductDTO createProduct(CreateProductRequest request, Long userId) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
        if (merchant == null) {
            throw new BusinessException(ErrorCode.MERCHANT_NOT_FOUND);
        }

        Product product = Product.builder()
                .merchantId(merchant.getId())
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .sales(0)
                .rating(BigDecimal.valueOf(5.00))
                .imageUrl(request.getImageUrl())
                .build();

        productMapper.insert(product);
        return toDTO(product);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, UpdateProductRequest request, Long userId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
        if (merchant == null || !product.getMerchantId().equals(merchant.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());

        productMapper.updateById(product);
        return toDTO(product);
    }

    @Transactional
    public void deleteProduct(Long id, Long userId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
        if (merchant == null || !product.getMerchantId().equals(merchant.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        productMapper.deleteById(id);
    }

    public String uploadImage(Long productId, MultipartFile file, Long userId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
        if (merchant == null || !product.getMerchantId().equals(merchant.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        String filename = UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
        String objectKey = "products/" + filename;

        try {
            String imageUrl = ossService.uploadFile(objectKey, file);
            product.setImageUrl(imageUrl);
            productMapper.updateById(product);
            return imageUrl;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return ".jpg";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : ".jpg";
    }

    private ProductDTO toDTO(Product product) {
        String merchantName = null;
        String categoryName = null;

        if (product.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectById(product.getMerchantId());
            if (merchant != null) {
                merchantName = merchant.getName();
            }
        }

        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        return ProductDTO.builder()
                .id(product.getId())
                .merchantId(product.getMerchantId())
                .merchantName(merchantName)
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .sales(product.getSales())
                .rating(product.getRating())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
