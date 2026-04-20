package com.shop.shop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateExpressRequest {
    @NotBlank
    private String expressCompany;

    @NotBlank
    private String trackingNumber;
}
