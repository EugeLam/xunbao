package com.shop.shop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAddressRequest {
    @NotBlank
    private String receiverName;

    @NotBlank
    private String phone;

    @NotBlank
    private String province;

    @NotBlank
    private String city;

    private String district;

    @NotBlank
    private String detailAddress;

    private Boolean isDefault;
}
