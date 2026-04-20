package com.shop.shop.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.LocalDateTime;

@TableName("reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @TableId
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("user_id")
    private Long userId;

    private Integer rating;

    private String content;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
