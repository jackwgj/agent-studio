package com.openjiuwen.studio.agent.manager.service.mcp.infrastructure.apig;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.CesMetricDataResp;
import com.openjiuwen.studio.agent.manager.rce.service.ClientService;
import com.openjiuwen.studio.agent.manager.service.mcp.properties.IamProperties;
import com.openjiuwen.studio.agent.manager.service.mcp.properties.IamSouthWestProperties;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;
import com.openjiuwen.studio.agent.manager.utils.McpServiceExceptionUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CesServiceTest {

    @Mock
    private ClientService clientService;

    @Mock
    private IamSouthWestProperties iamSouthWestProperties;

    @Mock
    private IamProperties iamProperties;

    @InjectMocks
    private CesService cesService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cesService, "southwestSwitch", "false");
        ReflectionTestUtils.setField(cesService, "hisAccountRotate", true);
        when(iamProperties.getProjectId()).thenReturn("test-project-id");
    }

    @Test
    void testMcpServicesCesByCountMetric_Success() {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("metric_name", "count");
        jsonBody.put("max", 100.0);
        jsonBody.put("min", 0.0);
        ResponseEntity<JSONObject> responseEntity = ResponseEntity.ok(jsonBody);

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<CommonUtil> commonUtilMock = mockStatic(CommonUtil.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            commonUtilMock.when(() -> CommonUtil.validateTenantIdIsNotEmpty()).thenAnswer(invocation -> null);

            when(clientService.queryMetricData(any(HttpHeaders.class), eq("test-project-id"),
                eq("SYS.FunctionGraph"), eq("count"), eq("1000"), eq("2000"), anyInt(), eq("sum"),
                anyString()))
                .thenReturn(responseEntity);

            CesMetricDataResp result = cesService.mcpServicesCesByCountMetric("1000", "2000", 300, "test-fg");
            assertNotNull(result);
        }
    }

    @Test
    void testMcpServicesCesByCountMetric_NullResponse_ReturnsNull() {
        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<CommonUtil> commonUtilMock = mockStatic(CommonUtil.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            commonUtilMock.when(() -> CommonUtil.validateTenantIdIsNotEmpty()).thenAnswer(invocation -> null);

            when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(null);

            CesMetricDataResp result = cesService.mcpServicesCesByCountMetric("1000", "2000", 300, "test-fg");
            assertNull(result);
        }
    }

    @Test
    void testMcpServicesCesByCountMetric_EmptyBody_ReturnsNull() {
        ResponseEntity<JSONObject> responseEntity = ResponseEntity.ok(null);

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<CommonUtil> commonUtilMock = mockStatic(CommonUtil.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            commonUtilMock.when(() -> CommonUtil.validateTenantIdIsNotEmpty()).thenAnswer(invocation -> null);

            when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(responseEntity);

            CesMetricDataResp result = cesService.mcpServicesCesByCountMetric("1000", "2000", 300, "test-fg");
            assertNull(result);
        }
    }

    @Test
    void testMcpServicesCesByCountMetric_Non2xxStatus_ReturnsNull() {
        ResponseEntity<JSONObject> responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<CommonUtil> commonUtilMock = mockStatic(CommonUtil.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            commonUtilMock.when(() -> CommonUtil.validateTenantIdIsNotEmpty()).thenAnswer(invocation -> null);

            when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(responseEntity);

            CesMetricDataResp result = cesService.mcpServicesCesByCountMetric("1000", "2000", 300, "test-fg");
            assertNull(result);
        }
    }

    @Test
    void testMcpServicesCesByFailRateMetric_Success() {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("metric_name", "failRate");
        jsonBody.put("max", 5.0);
        jsonBody.put("min", 0.0);
        ResponseEntity<JSONObject> responseEntity = ResponseEntity.ok(jsonBody);

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<CommonUtil> commonUtilMock = mockStatic(CommonUtil.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            commonUtilMock.when(() -> CommonUtil.validateTenantIdIsNotEmpty()).thenAnswer(invocation -> null);

            when(clientService.queryMetricData(any(HttpHeaders.class), eq("test-project-id"),
                eq("SYS.FunctionGraph"), eq("failRate"), eq("1000"), eq("2000"), anyInt(), eq("average"),
                anyString()))
                .thenReturn(responseEntity);

            CesMetricDataResp result = cesService.mcpServicesCesByFailRateMetric("1000", "2000", 300, "test-fg");
            assertNotNull(result);
        }
    }

    @Test
    void testMcpServicesCesByFailRateMetric_NullResponse_ReturnsNull() {
        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<CommonUtil> commonUtilMock = mockStatic(CommonUtil.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            commonUtilMock.when(() -> CommonUtil.validateTenantIdIsNotEmpty()).thenAnswer(invocation -> null);

            when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(null);

            CesMetricDataResp result = cesService.mcpServicesCesByFailRateMetric("1000", "2000", 300, "test-fg");
            assertNull(result);
        }
    }

    @Test
    void testMcpServicesCesByFailRateMetric_EmptyBody_ThrowsException() {
        ResponseEntity<JSONObject> responseEntity = ResponseEntity.ok(null);

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<CommonUtil> commonUtilMock = mockStatic(CommonUtil.class);
             MockedStatic<McpServiceExceptionUtils> exceptionMock = mockStatic(McpServiceExceptionUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            commonUtilMock.when(() -> CommonUtil.validateTenantIdIsNotEmpty()).thenAnswer(invocation -> null);
            exceptionMock.when(() -> McpServiceExceptionUtils.throwRemoteCallException(anyString(), anyString()))
                .thenThrow(new AgentStudioException(null));

            when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(responseEntity);

            assertThrows(AgentStudioException.class,
                () -> cesService.mcpServicesCesByFailRateMetric("1000", "2000", 300, "test-fg"));
        }
    }

    @Test
    void testMcpServicesCesByFailRateMetric_Non2xxStatus_ThrowsException() {
        ResponseEntity<JSONObject> responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<CommonUtil> commonUtilMock = mockStatic(CommonUtil.class);
             MockedStatic<McpServiceExceptionUtils> exceptionMock = mockStatic(McpServiceExceptionUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            commonUtilMock.when(() -> CommonUtil.validateTenantIdIsNotEmpty()).thenAnswer(invocation -> null);
            exceptionMock.when(() -> McpServiceExceptionUtils.throwRemoteCallException(anyString(), anyString()))
                .thenThrow(new AgentStudioException(null));

            when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(responseEntity);

            assertThrows(AgentStudioException.class,
                () -> cesService.mcpServicesCesByFailRateMetric("1000", "2000", 300, "test-fg"));
        }
    }
}
