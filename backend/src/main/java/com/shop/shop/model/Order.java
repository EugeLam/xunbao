package com.shop.shop.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @TableId
    private Long id;

    @TableField("buyer_id")
    private Long buyerId;

    @TableField("address_id")
    private Long addressId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    private String status;

    @TableField("express_company")
    private String expressCompany;

    @TableField("tracking_number")
    private String trackingNumber;

    @TableField("created_at")
    private LocalDateTime createdAt;

    public enum OrderStatus {
        PENDING,
        PAID,
        SHIPPED,
        COMPLETED,
        CANCELLED
    }
}
