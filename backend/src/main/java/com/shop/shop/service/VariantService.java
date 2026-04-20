package com.shop.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.shop.dto.CreateReviewRequest;
import com.shop.shop.dto.ReviewDTO;
import com.shop.shop.dto.VariantDTO;
import com.shop.shop.dto.CreateVariantRequest;
import com.shop.shop.exception.BusinessException;
import com.shop.shop.exception.ErrorCode;
import com.shop.shop.mapper.MerchantMapper;
import com.shop.shop.mapper.ProductMapper;
import com.shop.shop.mapper.ProductVariantMapper;
import com.shop.shop.mapper.ReviewMapper;
import com.shop.shop.mapper.UserMapper;
import com.shop.shop.model.Merchant;
import com.shop.shop.model.Product;
import com.shop.shop.model.ProductVariant;
import com.shop.shop.model.Review;
import com.shop.shop.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariantService {

    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;
    private final ProductVariantMapper variantMapper;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;

    public List<VariantDTO> getVariants(Long productId) {
        return variantMapper.selectList(
                        new LambdaQueryWrapper<ProductVariant>()
                                .eq(ProductVariant::getProductId, productId))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VariantDTO addVariant(Long productId, CreateVariantRequest request, Long userId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
        if (merchant == null) {
            throw new BusinessException(ErrorCode.MERCHANT_NOT_FOUND);
        }

        if (!product.getMerchantId().equals(merchant.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (variantMapper.selectCount(
                new LambdaQueryWrapper<ProductVariant>().eq(ProductVariant::getSku, request.getSku())) > 0) {
            throw new BusinessException(ErrorCode.SKU_ALREADY_EXISTS);
        }

        ProductVariant variant = ProductVariant.builder()
                .productId(productId)
                .sku(request.getSku())
                .attributes(request.getAttributes())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        variantMapper.insert(variant);
        return toDTO(variant);
    }

    public Page<ReviewDTO> getReviews(Long productId, int page, int size) {
        IPage<Review> reviewPage = reviewMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getProductId, productId)
                        .orderByDesc(Review::getCreatedAt));

        List<ReviewDTO> dtos = reviewPage.getRecords().stream()
                .map(this::toReviewDTO)
                .collect(Collectors.toList());

        Page<ReviewDTO> resultPage = new Page<>(page, size);
        resultPage.setRecords(dtos);
        resultPage.setTotal(reviewPage.getTotal());
        return resultPage;
    }

    @Transactional
    public ReviewDTO addReview(Long productId, CreateReviewRequest request, Long userId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Review review = Review.builder()
                .productId(productId)
                .userId(userId)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        reviewMapper.insert(review);

        updateProductRating(productId);

        return toReviewDTO(review);
    }

    private void updateProductRating(Long productId) {
        IPage<Review> reviewPage = reviewMapper.selectPage(Page.of(0, 1000),
                new LambdaQueryWrapper<Review>().eq(Review::getProductId, productId));

        if (reviewPage.getRecords().isEmpty()) return;

        BigDecimal avgRating = reviewPage.getRecords().stream()
                .map(r -> BigDecimal.valueOf(r.getRating()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(reviewPage.getRecords().size()), 2, BigDecimal.ROUND_HALF_UP);

        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setRating(avgRating);
            productMapper.updateById(product);
        }
    }

    private VariantDTO toDTO(ProductVariant variant) {
        return VariantDTO.builder()
                .id(variant.getId())
                .productId(variant.getProductId())
                .sku(variant.getSku())
                .attributes(variant.getAttributes())
                .price(variant.getPrice())
                .stock(variant.getStock())
                .build();
    }

    private ReviewDTO toReviewDTO(Review review) {
        com.shop.shop.model.User user = userMapper.selectById(review.getUserId());
        return ReviewDTO.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .userEmail(user != null ? user.getEmail() : null)
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
