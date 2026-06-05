/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.infrastructure.apig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.CesMetricDataResp;
import com.openjiuwen.studio.agent.manager.rce.service.ClientService;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsResp;
import com.openjiuwen.studio.agent.manager.service.mcp.properties.IamProperties;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
class CesServiceTest extends BaseTest {

    @Resource
    CesService cesService;

    private static MockedStatic<RequestContextUtils> mockedRequestContextUtils;

    @MockitoBean
    ClientService clientService;


    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
        mockedRequestContextUtils = Mockito.mockStatic(RequestContextUtils.class);
    }

    @AfterAll
    static void end() {
        mockedRequestContextUtils.close();
    }

    @Test
    void testMcpServicesCesByCountMetric() {
        IamProperties iamProperties = new IamProperties();
        iamProperties.setDomain("test");
        iamProperties.setUser("test");
        iamProperties.setProjectId("test");
        iamProperties.setProjectName("test");
        ReflectionTestUtils.setField(cesService, "iamProperties", iamProperties);
        ApigBindDomainsResp apigBindDomainsResp = new ApigBindDomainsResp();
        apigBindDomainsResp.setId("test1");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("metric_name", "test1");
        when(CommonUtil.getTenantId()).thenReturn("sysytem");
        ResponseEntity<JSONObject> responseEntity = new ResponseEntity<>(jsonObject, HttpStatusCode.valueOf(200));
        when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyString())).thenReturn(responseEntity);
        CesMetricDataResp result = cesService.mcpServicesCesByCountMetric("groupId", "test", 1, "fgName");
        Assertions.assertNotNull(result);
    }

    @Test
    void testMcpServicesCesByCountMetric2() {
        IamProperties iamProperties = new IamProperties();
        iamProperties.setDomain("test");
        iamProperties.setUser("test");
        iamProperties.setProjectId("test");
        iamProperties.setProjectName("test");
        ReflectionTestUtils.setField(cesService, "iamProperties", iamProperties);
        ApigBindDomainsResp apigBindDomainsResp = new ApigBindDomainsResp();
        apigBindDomainsResp.setId("test1");
        when(CommonUtil.getTenantId()).thenReturn("sysytem");
        ResponseEntity<JSONObject> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyString())).thenReturn(responseEntity);
        CesMetricDataResp result = cesService.mcpServicesCesByCountMetric("groupId", "test", 1, "fgName");
        Assertions.assertNull(result);
    }

    @Test
    void testMcpServicesCesByCountMetric3() {
        IamProperties iamProperties = new IamProperties();
        iamProperties.setDomain("test");
        iamProperties.setUser("test");
        iamProperties.setProjectId("test");
        iamProperties.setProjectName("test");
        when(CommonUtil.getTenantId()).thenReturn("sysytem");
        ReflectionTestUtils.setField(cesService, "iamProperties", iamProperties);
        when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyString())).thenReturn(null);
        CesMetricDataResp result = cesService.mcpServicesCesByCountMetric("groupId", "test", 1, "fgName");
        Assertions.assertNull(result);
    }

    @Test
    void testMcpServicesCesByCountMetric4() {
        IamProperties iamProperties = new IamProperties();
        iamProperties.setDomain("test");
        iamProperties.setUser("test");
        iamProperties.setProjectId("test");
        iamProperties.setProjectName("test");
        ReflectionTestUtils.setField(cesService, "iamProperties", iamProperties);
        ApigBindDomainsResp apigBindDomainsResp = new ApigBindDomainsResp();
        apigBindDomainsResp.setId("test1");
        when(CommonUtil.getTenantId()).thenReturn("sysytem");
        ResponseEntity<JSONObject> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(500));
        when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyString())).thenReturn(responseEntity);
        CesMetricDataResp result = cesService.mcpServicesCesByCountMetric("groupId", "test", 1, "fgName");
        Assertions.assertNull(result);
    }

    @Test
    void testMcpServicesCesByFailRateMetric() {
        IamProperties iamProperties = new IamProperties();
        iamProperties.setDomain("test");
        iamProperties.setUser("test");
        iamProperties.setProjectId("test");
        iamProperties.setProjectName("test");
        ReflectionTestUtils.setField(cesService, "iamProperties", iamProperties);
        ApigBindDomainsResp apigBindDomainsResp = new ApigBindDomainsResp();
        apigBindDomainsResp.setId("test1");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("metric_name", "test1");
        when(CommonUtil.getTenantId()).thenReturn("sysytem");
        ResponseEntity<JSONObject> responseEntity = new ResponseEntity<>(jsonObject, HttpStatusCode.valueOf(200));
        when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyString())).thenReturn(responseEntity);
        CesMetricDataResp result = cesService.mcpServicesCesByFailRateMetric("groupId", "test", 1, "fgName");
        Assertions.assertNotNull(result);
    }

    @Test
    void testMcpServicesCesByFailRateMetric2() {
        IamProperties iamProperties = new IamProperties();
        iamProperties.setDomain("test");
        iamProperties.setUser("test");
        iamProperties.setProjectId("test");
        iamProperties.setProjectName("test");
        ReflectionTestUtils.setField(cesService, "iamProperties", iamProperties);
        ApigBindDomainsResp apigBindDomainsResp = new ApigBindDomainsResp();
        apigBindDomainsResp.setId("test1");
        ResponseEntity<JSONObject> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyString())).thenReturn(responseEntity);
        Assertions.assertThrows(Exception.class,
            () -> cesService.mcpServicesCesByFailRateMetric("groupId", "test", 1, "fgName"));
    }

    @Test
    void testMcpServicesCesByFailRateMetric3() {
        IamProperties iamProperties = new IamProperties();
        iamProperties.setDomain("test");
        iamProperties.setUser("test");
        iamProperties.setProjectId("test");
        iamProperties.setProjectName("test");
        ReflectionTestUtils.setField(cesService, "iamProperties", iamProperties);
        when(CommonUtil.getTenantId()).thenReturn("sysytem");
        when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyString())).thenReturn(null);
        CesMetricDataResp result = cesService.mcpServicesCesByFailRateMetric("groupId", "test", 1, "fgName");
        Assertions.assertNull(result);
    }

    @Test
    void testMcpServicesCesByFailRateMetric4() {
        IamProperties iamProperties = new IamProperties();
        iamProperties.setDomain("test");
        iamProperties.setUser("test");
        iamProperties.setProjectId("test");
        iamProperties.setProjectName("test");
        ReflectionTestUtils.setField(cesService, "iamProperties", iamProperties);
        ResponseEntity<JSONObject> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(500));
        when(clientService.queryMetricData(any(HttpHeaders.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyString())).thenReturn(responseEntity);
        Assertions.assertThrows(Exception.class,
            () -> cesService.mcpServicesCesByFailRateMetric("groupId", "test", 1, "fgName"));
    }
}
