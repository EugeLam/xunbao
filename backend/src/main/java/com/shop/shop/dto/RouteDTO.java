package com.shop.shop.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDTO {
    private String acceptTime;
    private String acceptAddress;
    private String remark;
    private String status;
}
