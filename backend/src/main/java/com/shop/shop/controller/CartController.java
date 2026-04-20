package com.shop.shop.controller;

import com.shop.shop.dto.*;
import com.shop.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCart(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(
            @Valid @RequestBody AddCartRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.addToCart(request, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateCart(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.updateCart(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        cartService.removeFromCart(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Cart item removed", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@RequestAttribute Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
