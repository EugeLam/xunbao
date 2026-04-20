package com.shop.shop.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.LocalDateTime;

@TableName("users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @TableId
    private Long id;

    private String email;

    @TableField("password_hash")
    private String passwordHash;

    private String role;

    @TableField("created_at")
    private LocalDateTime createdAt;

    public enum UserRole {
        MERCHANT, BUYER
    }
}
