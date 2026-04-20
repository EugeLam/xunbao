package com.shop.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.shop.dto.AddFavoriteRequest;
import com.shop.shop.dto.FavoriteDTO;
import com.shop.shop.exception.BusinessException;
import com.shop.shop.exception.ErrorCode;
import com.shop.shop.mapper.FavoriteMapper;
import com.shop.shop.mapper.ProductMapper;
import com.shop.shop.mapper.UserMapper;
import com.shop.shop.model.Favorite;
import com.shop.shop.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    public List<FavoriteDTO> getFavorites(Long userId) {
        return favoriteMapper.selectList(
                        new LambdaQueryWrapper<Favorite>()
                                .eq(Favorite::getUserId, userId))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FavoriteDTO addFavorite(AddFavoriteRequest request, Long userId) {
        if (favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getProductId, request.getProductId())) > 0) {
            throw new BusinessException(ErrorCode.ALREADY_FAVORITED);
        }

        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Favorite favorite = Favorite.builder()
                .userId(userId)
                .productId(request.getProductId())
                .build();

        favoriteMapper.insert(favorite);
        favorite.setCreatedAt(java.time.LocalDateTime.now());
        return toDTO(favorite, product);
    }

    @Transactional
    public void removeFavorite(Long productId, Long userId) {
        favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getProductId, productId));
    }

    private FavoriteDTO toDTO(Favorite favorite, Product product) {
        return FavoriteDTO.builder()
                .id(favorite.getId())
                .productId(favorite.getProductId())
                .productName(product.getName())
                .productImage(product.getImageUrl())
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    private FavoriteDTO toDTO(Favorite favorite) {
        Product product = productMapper.selectById(favorite.getProductId());
        return FavoriteDTO.builder()
                .id(favorite.getId())
                .productId(favorite.getProductId())
                .productName(product != null ? product.getName() : null)
                .productImage(product != null ? product.getImageUrl() : null)
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
