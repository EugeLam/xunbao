package com.shop.shop.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @TableId
    private Long id;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("category_id")
    private Long categoryId;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private Integer sales;

    private BigDecimal rating;

    @TableField("image_url")
    private String imageUrl;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
