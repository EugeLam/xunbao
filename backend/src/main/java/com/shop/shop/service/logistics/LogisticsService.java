package com.shop.shop.service.logistics;

import com.shop.shop.dto.RouteDTO;
import com.shop.shop.dto.WaybillDTO;
import com.shop.shop.dto.WaybillRequest;

import java.util.List;

public interface LogisticsService {
    WaybillDTO createWaybill(WaybillRequest request);

    List<RouteDTO> getRoutes(String waybillNo);

    boolean cancelWaybill(String waybillNo);

    String getProviderCode();

    String getProviderName();

    boolean isAvailable();
}
