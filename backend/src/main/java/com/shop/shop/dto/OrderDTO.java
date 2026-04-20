package com.shop.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long buyerId;
    private String buyerEmail;
    private AddressDTO address;
    private BigDecimal totalAmount;
    private String status;
    private String expressCompany;
    private String trackingNumber;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private Long variantId;
        private String variantSku;
        private Integer quantity;
        private BigDecimal price;
    }
}
