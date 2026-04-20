package com.shop.shop.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.LocalDateTime;

@TableName("addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @TableId
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("receiver_name")
    private String receiverName;

    private String phone;

    private String province;

    private String city;

    private String district;

    @TableField("detail_address")
    private String detailAddress;

    @TableField("is_default")
    private Boolean isDefault;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
