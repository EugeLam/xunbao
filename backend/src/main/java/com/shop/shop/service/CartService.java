package com.shop.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.shop.dto.AddCartRequest;
import com.shop.shop.dto.CartItemDTO;
import com.shop.shop.dto.UpdateCartRequest;
import com.shop.shop.exception.BusinessException;
import com.shop.shop.exception.ErrorCode;
import com.shop.shop.mapper.CartItemMapper;
import com.shop.shop.mapper.ProductMapper;
import com.shop.shop.mapper.ProductVariantMapper;
import com.shop.shop.mapper.UserMapper;
import com.shop.shop.model.CartItem;
import com.shop.shop.model.Product;
import com.shop.shop.model.ProductVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final ProductVariantMapper variantMapper;
    private final UserMapper userMapper;

    public List<CartItemDTO> getCart(Long userId) {
        return cartItemMapper.selectList(
                        new LambdaQueryWrapper<CartItem>()
                                .eq(CartItem::getUserId, userId))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartItemDTO addToCart(AddCartRequest request, Long userId) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = variantMapper.selectById(request.getVariantId());
            if (variant == null) {
                throw new BusinessException(ErrorCode.VARIANT_NOT_FOUND);
            }
        }

        CartItem existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getProductId, request.getProductId())
                        .eq(request.getVariantId() != null, CartItem::getVariantId, request.getVariantId()));

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            cartItemMapper.updateById(existing);
            return toDTO(existing, product, variant);
        }

        CartItem item = CartItem.builder()
                .userId(userId)
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .quantity(request.getQuantity())
                .build();

        cartItemMapper.insert(item);
        return toDTO(item, product, variant);
    }

    @Transactional
    public CartItemDTO updateCart(Long id, UpdateCartRequest request, Long userId) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        item.setQuantity(request.getQuantity());
        cartItemMapper.updateById(item);

        Product product = productMapper.selectById(item.getProductId());
        ProductVariant variant = item.getVariantId() != null ? variantMapper.selectById(item.getVariantId()) : null;
        return toDTO(item, product, variant);
    }

    @Transactional
    public void removeFromCart(Long id, Long userId) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        cartItemMapper.deleteById(id);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }

    private CartItemDTO toDTO(CartItem item, Product product, ProductVariant variant) {
        BigDecimal price = variant != null ? variant.getPrice() : product.getPrice();
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(product != null ? product.getName() : null)
                .variantId(item.getVariantId())
                .variantSku(variant != null ? variant.getSku() : null)
                .quantity(item.getQuantity())
                .price(price)
                .subtotal(subtotal)
                .build();
    }

    private CartItemDTO toDTO(CartItem item) {
        Product product = productMapper.selectById(item.getProductId());
        ProductVariant variant = item.getVariantId() != null ? variantMapper.selectById(item.getVariantId()) : null;
        return toDTO(item, product, variant);
    }
}
