package com.shop.shop.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("merchants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {
    @TableId
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String name;

    private BigDecimal rating;

    @TableField("total_sales")
    private Integer totalSales;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
