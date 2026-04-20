package com.shop.shop.controller;

import com.shop.shop.dto.*;
import com.shop.shop.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteDTO>>> getFavorites(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(favoriteService.getFavorites(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteDTO>> addFavorite(
            @Valid @RequestBody AddFavoriteRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(favoriteService.addFavorite(request, userId)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long productId,
            @RequestAttribute Long userId) {
        favoriteService.removeFavorite(productId, userId);
        return ResponseEntity.ok(ApiResponse.success("Favorite removed", null));
    }
}
