package com.shop.shop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFavoriteRequest {
    @NotNull
    private Long productId;
}
