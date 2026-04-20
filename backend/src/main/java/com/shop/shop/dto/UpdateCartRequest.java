package com.shop.shop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateCartRequest {
    @NotNull
    @Positive
    private Integer quantity;
}
