package com.shop.shop.service.logistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sf.csim.express.service.CallExpressServiceTools;
import com.sf.csim.express.service.HttpClientUtil;
import com.sf.csim.express.service.IServiceCodeStandard;
import com.sf.csim.express.service.code.ExpressServiceCodeEnum;
import com.shop.shop.dto.RouteDTO;
import com.shop.shop.dto.WaybillDTO;
import com.shop.shop.dto.WaybillRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SfExpressAdapter implements LogisticsService {

    @Value("${logistics.sf.code:}")
    private String code;

    @Value("${logistics.sf.check-word:}")
    private String checkWord;

    @Value("${logistics.sf.sandbox:true}")
    private boolean sandbox;

    private static final String SANDBOX_URL = "https://sfapi-sbox.sf-express.com/std/service";
    private static final String PROD_URL = "https://sfapi.sf-express.com/std/service";

    private final CallExpressServiceTools sfTools = CallExpressServiceTools.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    @Override
    public WaybillDTO createWaybill(WaybillRequest request) {
        log.info("SF isConfigured: {}, code: {}", isConfigured(), code);
        if (!isConfigured()) {
            log.warn("SF Express API not configured, returning mock waybill");
            return createMockWaybill(request);
        }

        log.info("Creating SF waybill for order: {}", request.getOrderId());
        try {
            String msgData = buildCreateOrderJson(request);
            Map<String, String> formData = buildFormRequest(ExpressServiceCodeEnum.EXP_RECE_CREATE_ORDER, msgData);

            String response = HttpClientUtil.post(getBaseUrl(), formData);

            log.info("SF createWaybill response: {}", response);
            return parseCreateWaybillResponse(response, request);
        } catch (Exception e) {
            log.error("Failed to create SF waybill: {}", e.getMessage(), e);
            return createMockWaybill(request);
        }
    }

    @Override
    public List<RouteDTO> getRoutes(String waybillNo) {
        if (!isConfigured()) {
            log.warn("SF Express API not configured");
            return Collections.emptyList();
        }

        log.info("Querying SF routes for waybill: {}", waybillNo);
        try {
            String msgData = buildSearchRoutesJson(waybillNo);
            Map<String, String> formData = buildFormRequest(ExpressServiceCodeEnum.EXP_RECE_SEARCH_ROUTES, msgData);

            String response = HttpClientUtil.post(getBaseUrl(), formData);

            log.info("SF getRoutes response: {}", response);
            return parseRoutesResponse(response);
        } catch (Exception e) {
            log.error("Failed to query SF routes: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean cancelWaybill(String waybillNo) {
        if (!isConfigured()) {
            log.warn("SF Express API not configured");
            return false;
        }

        log.info("Canceling SF waybill: {}", waybillNo);
        try {
            String msgData = buildUpdateOrderJson(waybillNo, 2);
            Map<String, String> formData = buildFormRequest(ExpressServiceCodeEnum.EXP_RECE_UPDATE_ORDER, msgData);

            String response = HttpClientUtil.post(getBaseUrl(), formData);

            log.info("SF cancelWaybill response: {}", response);
            return parseCancelResponse(response);
        } catch (Exception e) {
            log.error("Failed to cancel SF waybill: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getProviderCode() {
        return "SF";
    }

    @Override
    public String getProviderName() {
        return "顺丰速运";
    }

    @Override
    public boolean isAvailable() {
        return isConfigured();
    }

    private boolean isConfigured() {
        return code != null && !code.isBlank()
                && checkWord != null && !checkWord.isBlank();
    }

    private String getBaseUrl() {
        return sandbox ? SANDBOX_URL : PROD_URL;
    }

    private Map<String, String> buildFormRequest(IServiceCodeStandard serviceCode, String msgData) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String msgDigest = sfTools.getMsgDigest(msgData, timestamp, checkWord);

        Map<String, String> formData = new HashMap<>();
        formData.put("partnerID", code);
        formData.put("requestID", java.util.UUID.randomUUID().toString().replace("-", ""));
        formData.put("serviceCode", serviceCode.getCode());
        formData.put("timestamp", timestamp);
        formData.put("msgData", msgData);
        formData.put("msgDigest", msgDigest);

        return formData;
    }

    private String buildCreateOrderJson(WaybillRequest request) throws Exception {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("language", "zh-CN");
        orderData.put("orderId", request.getOrderId());
        orderData.put("payMethod", request.getPayMethod() != null ? request.getPayMethod() : 1);

        if (request.getContactInfoList() != null && !request.getContactInfoList().isEmpty()) {
            orderData.put("contactInfoList", request.getContactInfoList());
        } else {
            List<Map<String, Object>> contactInfoList = new ArrayList<>();
            if (request.getSenderName() != null) {
                Map<String, Object> sender = new HashMap<>();
                sender.put("contactType", 1);
                sender.put("contact", request.getSenderName());
                sender.put("tel", request.getSenderPhone());
                sender.put("province", request.getSenderProvince());
                sender.put("city", request.getSenderCity());
                sender.put("county", request.getSenderDistrict());
                sender.put("address", request.getSenderAddress());
                sender.put("country", "CN");
                contactInfoList.add(sender);
            }
            if (request.getRecipientName() != null) {
                Map<String, Object> receiver = new HashMap<>();
                receiver.put("contactType", 2);
                receiver.put("contact", request.getRecipientName());
                receiver.put("tel", request.getRecipientPhone());
                receiver.put("province", request.getRecipientProvince());
                receiver.put("city", request.getRecipientCity());
                receiver.put("county", request.getRecipientDistrict());
                receiver.put("address", request.getRecipientAddress());
                receiver.put("country", "CN");
                contactInfoList.add(receiver);
            }
            orderData.put("contactInfoList", contactInfoList);
        }

        if (request.getCargoDetails() != null && !request.getCargoDetails().isEmpty()) {
            orderData.put("cargoDetails", request.getCargoDetails());
        } else {
            List<Map<String, Object>> cargoDetails = new ArrayList<>();
            Map<String, Object> cargo = new HashMap<>();
            cargo.put("name", "商品");
            cargo.put("count", 1);
            cargoDetails.add(cargo);
            orderData.put("cargoDetails", cargoDetails);
        }

        if (request.getRemark() != null) {
            orderData.put("remark", request.getRemark());
        }

        return objectMapper.writeValueAsString(orderData);
    }

    private String buildSearchRoutesJson(String waybillNo) throws Exception {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("language", 0);
        requestData.put("trackingType", 1);
        List<String> trackingNumbers = new ArrayList<>();
        trackingNumbers.add(waybillNo);
        requestData.put("trackingNumber", trackingNumbers);
        requestData.put("methodType", 1);

        return objectMapper.writeValueAsString(requestData);
    }

    private String buildUpdateOrderJson(String waybillNo, int dealType) throws Exception {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("orderId", waybillNo);
        requestData.put("dealType", dealType);

        return objectMapper.writeValueAsString(requestData);
    }

    private WaybillDTO parseCreateWaybillResponse(String response, WaybillRequest request) {
        try {
            Map<String, Object> resp = objectMapper.readValue(response, Map.class);
            String apiResultData = (String) resp.get("apiResultData");
            if (apiResultData != null) {
                Map<String, Object> resultData = objectMapper.readValue(apiResultData, Map.class);
                Object success = resultData.get("success");
                if (Boolean.TRUE.equals(success) || "true".equals(success)) {
                    // 优先从waybillNoInfoList获取
                    Object waybillNoInfoListObj = resultData.get("waybillNoInfoList");
                    String waybillNo = null;
                    if (waybillNoInfoListObj instanceof List) {
                        List<?> list = (List<?>) waybillNoInfoListObj;
                        if (!list.isEmpty() && list.get(0) instanceof Map) {
                            Map<?, ?> waybillInfo = (Map<?, ?>) list.get(0);
                            waybillNo = (String) waybillInfo.get("waybillNo");
                        }
                    }
                    // 备用：从waybillNo获取
                    if (waybillNo == null || waybillNo.isBlank()) {
                        Object waybillNoObj = resultData.get("waybillNo");
                        waybillNo = waybillNoObj != null ? waybillNoObj.toString() : null;
                    }
                    // 备用：从waybillNoList获取
                    if (waybillNo == null || waybillNo.isBlank()) {
                        Object waybillNosObj = resultData.get("waybillNoList");
                        if (waybillNosObj instanceof List) {
                            List<?> list = (List<?>) waybillNosObj;
                            if (!list.isEmpty()) {
                                waybillNo = list.get(0).toString();
                            }
                        }
                    }
                    if (waybillNo == null || waybillNo.isBlank()) {
                        waybillNo = "SF" + System.currentTimeMillis();
                    }
                    return WaybillDTO.builder()
                            .waybillNo(waybillNo)
                            .status("CREATED")
                            .orderNo(request.getOrderId())
                            .expressCompany("顺丰速运")
                            .build();
                }
            }
            log.warn("SF API returned error: {}", response);
            return createMockWaybill(request);
        } catch (Exception e) {
            log.error("Failed to parse SF response: {}", e.getMessage(), e);
            return createMockWaybill(request);
        }
    }

    private List<RouteDTO> parseRoutesResponse(String response) {
        List<RouteDTO> routes = new ArrayList<>();
        try {
            Map<String, Object> resp = objectMapper.readValue(response, Map.class);
            String apiResultData = (String) resp.get("apiResultData");
            if (apiResultData != null) {
                Map<String, Object> resultData = objectMapper.readValue(apiResultData, Map.class);
                Object routesObj = resultData.get("routes");
                if (routesObj instanceof List) {
                    List<?> routesList = (List<?>) routesObj;
                    for (Object route : routesList) {
                        if (route instanceof Map) {
                            Map<String, Object> routeMap = (Map<String, Object>) route;
                            RouteDTO dto = RouteDTO.builder()
                                    .acceptTime((String) routeMap.get("acceptTime"))
                                    .acceptAddress((String) routeMap.get("acceptAddress"))
                                    .remark((String) routeMap.get("remark"))
                                    .status((String) routeMap.get("status"))
                                    .build();
                            routes.add(dto);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse routes response: {}", e.getMessage(), e);
        }
        return routes;
    }

    private boolean parseCancelResponse(String response) {
        try {
            Map<String, Object> resp = objectMapper.readValue(response, Map.class);
            String apiResultData = (String) resp.get("apiResultData");
            if (apiResultData != null) {
                Map<String, Object> resultData = objectMapper.readValue(apiResultData, Map.class);
                Object success = resultData.get("success");
                return Boolean.TRUE.equals(success) || "true".equals(success);
            }
        } catch (Exception e) {
            log.error("Failed to parse cancel response: {}", e.getMessage(), e);
        }
        return false;
    }

    private WaybillDTO createMockWaybill(WaybillRequest request) {
        String waybillNo = "SF" + System.currentTimeMillis();
        return WaybillDTO.builder()
                .waybillNo(waybillNo)
                .status("CREATE_SUCCESS")
                .orderNo(request.getOrderId())
                .expressCompany("顺丰速运")
                .build();
    }
}
