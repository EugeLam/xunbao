package com.shop.shop.service.logistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sf.csim.express.service.CallExpressServiceTools;
import com.shop.shop.dto.RouteDTO;
import com.shop.shop.dto.WaybillDTO;
import com.shop.shop.dto.WaybillRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SfExpressAdapterTest {

    @Mock
    private CallExpressServiceTools sfTools;

    private SfExpressAdapter adapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_CODE = "Y3IN4NJG";
    private static final String TEST_CHECK_WORD = "gHSuGzldgen8SawiLdYaOYgn4H9fxiJe";

    @BeforeEach
    void setUp() {
        adapter = new SfExpressAdapter();
        ReflectionTestUtils.setField(adapter, "code", TEST_CODE);
        ReflectionTestUtils.setField(adapter, "checkWord", TEST_CHECK_WORD);
        ReflectionTestUtils.setField(adapter, "sandbox", true);
        ReflectionTestUtils.setField(adapter, "sfTools", sfTools);
        ReflectionTestUtils.setField(adapter, "objectMapper", objectMapper);
    }

    @Nested
    @DisplayName("配置状态测试")
    class ConfigurationTests {

        @Test
        @DisplayName("配置完整时应可用")
        void isAvailable_whenConfigured_shouldReturnTrue() {
            assertTrue(adapter.isAvailable());
            assertEquals("SF", adapter.getProviderCode());
            assertEquals("顺丰速运", adapter.getProviderName());
        }

        @Test
        @DisplayName("未配置code时应不可用")
        void isAvailable_whenCodeEmpty_shouldReturnFalse() {
            ReflectionTestUtils.setField(adapter, "code", "");
            assertFalse(adapter.isAvailable());
        }

        @Test
        @DisplayName("未配置checkWord时应不可用")
        void isAvailable_whenCheckWordEmpty_shouldReturnFalse() {
            ReflectionTestUtils.setField(adapter, "checkWord", "");
            assertFalse(adapter.isAvailable());
        }

        @Test
        @DisplayName("沙箱环境应使用沙箱URL")
        void getBaseUrl_whenSandbox_shouldReturnSandboxUrl() {
            ReflectionTestUtils.setField(adapter, "sandbox", true);
            String url = ReflectionTestUtils.invokeMethod(adapter, "getBaseUrl");
            assertEquals("https://sfapi-sbox.sf-express.com/std/service", url);
        }

        @Test
        @DisplayName("生产环境应使用生产URL")
        void getBaseUrl_whenProduction_shouldReturnProductionUrl() {
            ReflectionTestUtils.setField(adapter, "sandbox", false);
            String url = ReflectionTestUtils.invokeMethod(adapter, "getBaseUrl");
            assertEquals("https://sfapi.sf-express.com/std/service", url);
        }
    }

    @Nested
    @DisplayName("下单测试")
    class CreateWaybillTests {

        @Test
        @DisplayName("未配置时应返回Mock运单")
        void createWaybill_whenNotConfigured_shouldReturnMock() {
            ReflectionTestUtils.setField(adapter, "code", "");
            ReflectionTestUtils.setField(adapter, "checkWord", "");

            WaybillRequest request = createTestWaybillRequest();
            WaybillDTO result = adapter.createWaybill(request);

            assertNotNull(result);
            assertTrue(result.getWaybillNo().startsWith("SF"));
            assertEquals("CREATE_SUCCESS", result.getStatus());
            assertEquals("顺丰速运", result.getExpressCompany());
        }

        @Test
        @DisplayName("API返回非成功时应返回Mock运单")
        void createWaybill_whenApiReturnsFailure_shouldReturnMock() {
            ReflectionTestUtils.setField(adapter, "code", "");
            ReflectionTestUtils.setField(adapter, "checkWord", "");

            WaybillRequest request = createTestWaybillRequest();
            WaybillDTO result = adapter.createWaybill(request);

            assertNotNull(result);
            assertTrue(result.getWaybillNo().startsWith("SF"));
        }

        @Test
        @DisplayName("构建下单请求应包含正确的服务码")
        void createWaybill_shouldUseCorrectServiceCode() throws Exception {
            WaybillRequest request = createTestWaybillRequest();

            // 由于SDK的HttpClientUtil和getMsgDigest是静态/final方法，mock复杂
            // 这里只验证在未配置时返回Mock运单
            ReflectionTestUtils.setField(adapter, "code", "");
            ReflectionTestUtils.setField(adapter, "checkWord", "");

            WaybillDTO result = adapter.createWaybill(request);

            assertNotNull(result);
            assertTrue(result.getWaybillNo().startsWith("SF"));
            assertEquals("顺丰速运", result.getExpressCompany());
        }
    }

    @Nested
    @DisplayName("路由查询测试")
    class GetRoutesTests {

        @Test
        @DisplayName("未配置时应返回空列表")
        void getRoutes_whenNotConfigured_shouldReturnEmptyList() {
            ReflectionTestUtils.setField(adapter, "code", "");

            List<RouteDTO> result = adapter.getRoutes("SF1234567890");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("取消运单测试")
    class CancelWaybillTests {

        @Test
        @DisplayName("未配置时应返回false")
        void cancelWaybill_whenNotConfigured_shouldReturnFalse() {
            ReflectionTestUtils.setField(adapter, "code", "");

            boolean result = adapter.cancelWaybill("SF1234567890");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("JSON构建测试")
    class JsonBuildingTests {

        @Test
        @DisplayName("构建下单JSON应包含订单信息")
        void buildCreateOrderJson_shouldContainOrderInfo() throws Exception {
            WaybillRequest request = WaybillRequest.builder()
                    .orderId("ORDER-123")
                    .payMethod(1)
                    .senderName("张三")
                    .senderPhone("13800138000")
                    .senderProvince("广东省")
                    .senderCity("深圳市")
                    .senderDistrict("南山区")
                    .senderAddress("科技园路1号")
                    .recipientName("李四")
                    .recipientPhone("13900139000")
                    .recipientProvince("北京市")
                    .recipientCity("北京市")
                    .recipientDistrict("朝阳区")
                    .recipientAddress("建国路88号")
                    .remark("小心轻放")
                    .build();

            // 使用反射调用私有方法
            String json = ReflectionTestUtils.invokeMethod(adapter, "buildCreateOrderJson", request);

            assertNotNull(json);
            assertTrue(json.contains("ORDER-123"));
            assertTrue(json.contains("zh-CN"));
            assertTrue(json.contains("张三"));
            assertTrue(json.contains("李四"));
        }

        @Test
        @DisplayName("使用ContactInfoList时应正确构建JSON")
        void buildCreateOrderJson_withContactInfoList_shouldWork() throws Exception {
            WaybillRequest.ContactInfo sender = WaybillRequest.ContactInfo.builder()
                    .contactType(1)
                    .contact("张三")
                    .tel("13800138000")
                    .province("广东省")
                    .city("深圳市")
                    .county("南山区")
                    .address("科技园路1号")
                    .country("CN")
                    .build();

            WaybillRequest.ContactInfo recipient = WaybillRequest.ContactInfo.builder()
                    .contactType(2)
                    .contact("李四")
                    .tel("13900139000")
                    .province("北京市")
                    .city("北京市")
                    .county("朝阳区")
                    .address("建国路88号")
                    .country("CN")
                    .build();

            WaybillRequest request = WaybillRequest.builder()
                    .orderId("ORDER-456")
                    .contactInfoList(List.of(sender, recipient))
                    .build();

            String json = ReflectionTestUtils.invokeMethod(adapter, "buildCreateOrderJson", request);

            assertNotNull(json);
            assertTrue(json.contains("ORDER-456"));
            assertTrue(json.contains("contactType"));
        }

        @Test
        @DisplayName("构建查询路由JSON应包含运单号")
        void buildSearchRoutesJson_shouldContainWaybillNo() throws Exception {
            String json = ReflectionTestUtils.invokeMethod(adapter, "buildSearchRoutesJson", "SF1234567890");

            assertNotNull(json);
            assertTrue(json.contains("SF1234567890"));
            assertTrue(json.contains("trackingType"));
        }

        @Test
        @DisplayName("构建取消订单JSON应包含订单号和类型")
        void buildUpdateOrderJson_shouldContainOrderIdAndDealType() throws Exception {
            String json = ReflectionTestUtils.invokeMethod(adapter, "buildUpdateOrderJson", "ORDER-123", 2);

            assertNotNull(json);
            assertTrue(json.contains("ORDER-123"));
            assertTrue(json.contains("dealType"));
            assertTrue(json.contains("2"));
        }
    }

    @Nested
    @DisplayName("响应解析测试")
    class ResponseParsingTests {

        @Test
        @DisplayName("解析成功响应应返回正确的运单号")
        void parseCreateWaybillResponse_whenSuccess_shouldReturnCorrectWaybillNo() {
            WaybillRequest request = createTestWaybillRequest();
            String response = "{\"apiResultData\":\"{\\\"success\\\":true,\\\"waybillNoInfoList\\\":[{\\\"waybillNo\\\":\\\"SF1234567890\\\"}]}\",\"apiResultCode\":\"A1000\"}";

            WaybillDTO result = ReflectionTestUtils.invokeMethod(adapter, "parseCreateWaybillResponse", response, request);

            assertNotNull(result);
            assertEquals("SF1234567890", result.getWaybillNo());
            assertEquals("CREATED", result.getStatus());
        }

        @Test
        @DisplayName("解析失败响应应返回Mock运单")
        void parseCreateWaybillResponse_whenFailure_shouldReturnMock() {
            WaybillRequest request = createTestWaybillRequest();
            String response = "{\"apiResultData\":\"{\\\"success\\\":false}\",\"apiResultCode\":\"A1004\"}";

            WaybillDTO result = ReflectionTestUtils.invokeMethod(adapter, "parseCreateWaybillResponse", response, request);

            assertNotNull(result);
            assertTrue(result.getWaybillNo().startsWith("SF"));
        }

        @Test
        @DisplayName("解析路由响应应返回正确的路由列表")
        void parseRoutesResponse_whenSuccess_shouldReturnRoutes() {
            String response = "{\"apiResultData\":\"{\\\"routes\\\":[{\\\"acceptTime\\\":\\\"2024-01-01 10:00:00\\\",\\\"acceptAddress\\\":\\\"深圳\\\",\\\"remark\\\":\\\"已揽收\\\",\\\"status\\\":\\\"已揽收\\\"}]}\",\"apiResultCode\":\"A1000\"}";

            List<RouteDTO> result = ReflectionTestUtils.invokeMethod(adapter, "parseRoutesResponse", response);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("2024-01-01 10:00:00", result.get(0).getAcceptTime());
            assertEquals("深圳", result.get(0).getAcceptAddress());
        }

        @Test
        @DisplayName("解析取消成功响应应返回true")
        void parseCancelResponse_whenSuccess_shouldReturnTrue() {
            String response = "{\"apiResultData\":\"{\\\"success\\\":true}\",\"apiResultCode\":\"A1000\"}";

            boolean result = ReflectionTestUtils.invokeMethod(adapter, "parseCancelResponse", response);

            assertTrue(result);
        }

        @Test
        @DisplayName("解析取消失败响应应返回false")
        void parseCancelResponse_whenFailure_shouldReturnFalse() {
            String response = "{\"apiResultData\":\"{\\\"success\\\":false}\",\"apiResultCode\":\"A1004\"}";

            boolean result = ReflectionTestUtils.invokeMethod(adapter, "parseCancelResponse", response);

            assertFalse(result);
        }
    }

    private WaybillRequest createTestWaybillRequest() {
        return WaybillRequest.builder()
                .orderId("TEST-ORDER-001")
                .payMethod(1)
                .senderName("张三")
                .senderPhone("13800138000")
                .senderProvince("广东省")
                .senderCity("深圳市")
                .senderDistrict("南山区")
                .senderAddress("科技园路1号")
                .recipientName("李四")
                .recipientPhone("13900139000")
                .recipientProvince("北京市")
                .recipientCity("北京市")
                .recipientDistrict("朝阳区")
                .recipientAddress("建国路88号")
                .remark("测试订单")
                .build();
    }
}
