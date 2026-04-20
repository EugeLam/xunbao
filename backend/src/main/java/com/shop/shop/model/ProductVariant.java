package com.shop.shop.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    @TableId
    private Long id;

    @TableField("product_id")
    private Long productId;

    private String sku;

    private String attributes;

    private BigDecimal price;

    private Integer stock;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
