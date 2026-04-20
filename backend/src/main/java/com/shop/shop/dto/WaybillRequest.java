package com.shop.shop.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaybillRequest {
    private String language;
    private String orderId;
    private Integer payMethod;
    private String cargoDesc;
    private String monthlyCard;
    private Integer expressTypeId;
    private Integer parcelQty;
    private String remark;
    private List<ContactInfo> contactInfoList;
    private List<CargoDetail> cargoDetails;

    // Individual sender/recipient fields (for backward compatibility with OrderService)
    private String senderName;
    private String senderPhone;
    private String senderProvince;
    private String senderCity;
    private String senderDistrict;
    private String senderAddress;
    private String recipientName;
    private String recipientPhone;
    private String recipientProvince;
    private String recipientCity;
    private String recipientDistrict;
    private String recipientAddress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactInfo {
        private Integer contactType;
        private String contact;
        private String tel;
        private String mobile;
        private String country;
        private String province;
        private String city;
        private String county;
        private String address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CargoDetail {
        private String name;
        private Double count;
        private String unit;
    }
}
