package com.shop.shop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddCartRequest {
    @NotNull
    private Long productId;

    private Long variantId;

    @NotNull
    @Positive
    private Integer quantity;
}
