package com.shop.shop.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaybillDTO {
    private String waybillNo;
    private String status;
    private String orderNo;
    private String expressCompany;
    private LocalDateTime createTime;
}
