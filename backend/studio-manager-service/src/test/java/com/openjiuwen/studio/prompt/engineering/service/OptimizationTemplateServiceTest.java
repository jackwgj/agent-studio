/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.dto.OptimizationJobInfo;
import com.openjiuwen.studio.prompt.engineering.dto.TemplateOptimizeProgressResp;
import com.openjiuwen.studio.prompt.engineering.dto.TemplateOptimizeReq;
import com.openjiuwen.studio.prompt.engineering.dto.TemplateOptimizeResp;
import com.openjiuwen.studio.prompt.engineering.enums.ResponseCode;
import com.openjiuwen.studio.prompt.engineering.remote.ClientTemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;

/**
 * OptimizationTemplateService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OptimizationTemplateServiceTest {

    @InjectMocks
    private OptimizationTemplateService optimizationTemplateService;

    @Mock
    private ClientTemplate clientTemplate;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(optimizationTemplateService, "jiuwenBaseUrl", "http://localhost:10001");
    }

    // ========== createOptimization ==========

    @Test
    void testCreateOptimization_Success() {
        TemplateOptimizeReq req = TemplateOptimizeReq.builder()
            .name("优化任务")
            .build();

        OptimizationJobInfo jobInfo = new OptimizationJobInfo();
        jobInfo.setId("jiuwen-001");

        TemplateOptimizeResp resp = TemplateOptimizeResp.builder()
            .code(ResponseCode.OK.getCode())
            .jobInfo(jobInfo)
            .build();

        ResponseEntity<TemplateOptimizeResp> responseEntity = new ResponseEntity<>(resp, HttpStatus.OK);
        when(clientTemplate.postForEntity(anyString(), anyString(), anyString(), eq(TemplateOptimizeResp.class)))
            .thenReturn(responseEntity);

        TemplateOptimizeResp result = optimizationTemplateService.createOptimization(req, "token");
        assertNotNull(result);
    }

    @Test
    void testCreateOptimization_Non2xx_Throws() {
        TemplateOptimizeReq req = TemplateOptimizeReq.builder().build();
        ResponseEntity<TemplateOptimizeResp> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(clientTemplate.postForEntity(anyString(), anyString(), anyString(), eq(TemplateOptimizeResp.class)))
            .thenReturn(responseEntity);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTemplateService.createOptimization(req, "token"));
        assertEquals(StudioError.OPTIMIZATION_TASK_FROM_JIUWEN_SERVICE_ERROR, exception.getErrorCode());
    }

    @Test
    void testCreateOptimization_NullBody_Throws() {
        TemplateOptimizeReq req = TemplateOptimizeReq.builder().build();
        ResponseEntity<TemplateOptimizeResp> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        when(clientTemplate.postForEntity(anyString(), anyString(), anyString(), eq(TemplateOptimizeResp.class)))
            .thenReturn(responseEntity);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTemplateService.createOptimization(req, "token"));
        assertEquals(StudioError.CREATE_OPTIMIZATION_TASK_RESP_NULL, exception.getErrorCode());
    }

    @Test
    void testCreateOptimization_NonOKCode_Throws() {
        TemplateOptimizeReq req = TemplateOptimizeReq.builder().build();
        TemplateOptimizeResp resp = TemplateOptimizeResp.builder()
            .code(ResponseCode.INTERNAL_SERVER_ERROR.getCode())
            .message("error")
            .build();
        ResponseEntity<TemplateOptimizeResp> responseEntity = new ResponseEntity<>(resp, HttpStatus.OK);
        when(clientTemplate.postForEntity(anyString(), anyString(), anyString(), eq(TemplateOptimizeResp.class)))
            .thenReturn(responseEntity);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTemplateService.createOptimization(req, "token"));
        assertEquals(StudioError.OPTIMIZE_TEMPLATE_FAILED, exception.getErrorCode());
    }

    @Test
    void testCreateOptimization_ResourceAccessException_Throws() {
        TemplateOptimizeReq req = TemplateOptimizeReq.builder().build();
        when(clientTemplate.postForEntity(anyString(), anyString(), anyString(), eq(TemplateOptimizeResp.class)))
            .thenThrow(new ResourceAccessException("Connection refused"));

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTemplateService.createOptimization(req, "token"));
        assertEquals(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED, exception.getErrorCode());
    }

    // ========== changeOptimization ==========

    @Test
    void testChangeOptimization_Restart() {
        TemplateOptimizeResp resp = TemplateOptimizeResp.builder()
            .code(ResponseCode.OK.getCode())
            .build();
        ResponseEntity<TemplateOptimizeResp> responseEntity = new ResponseEntity<>(resp, HttpStatus.OK);
        when(clientTemplate.postForEntity(anyString(), anyString(), eq(null), eq(TemplateOptimizeResp.class)))
            .thenReturn(responseEntity);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            TemplateOptimizeResp result = optimizationTemplateService.changeOptimization("jiuwen-001", true);
            assertNotNull(result);
        }
    }

    @Test
    void testChangeOptimization_Stop() {
        TemplateOptimizeResp resp = TemplateOptimizeResp.builder()
            .code(ResponseCode.OK.getCode())
            .build();
        ResponseEntity<TemplateOptimizeResp> responseEntity = new ResponseEntity<>(resp, HttpStatus.OK);
        when(clientTemplate.postForEntity(anyString(), anyString(), eq(null), eq(TemplateOptimizeResp.class)))
            .thenReturn(responseEntity);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            TemplateOptimizeResp result = optimizationTemplateService.changeOptimization("jiuwen-001", false);
            assertNotNull(result);
        }
    }

    @Test
    void testChangeOptimization_Non2xx_Throws() {
        ResponseEntity<TemplateOptimizeResp> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(clientTemplate.postForEntity(anyString(), anyString(), eq(null), eq(TemplateOptimizeResp.class)))
            .thenReturn(responseEntity);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            AgentStudioException exception = assertThrows(AgentStudioException.class,
                () -> optimizationTemplateService.changeOptimization("jiuwen-001", true));
            assertEquals(StudioError.OPTIMIZATION_TASK_OPERATION_FAILED, exception.getErrorCode());
        }
    }

    // ========== deleteOptimization ==========

    @Test
    void testDeleteOptimization_Success() {
        TemplateOptimizeResp resp = TemplateOptimizeResp.builder()
            .code(ResponseCode.OK.getCode())
            .build();
        ResponseEntity<TemplateOptimizeResp> responseEntity = new ResponseEntity<>(resp, HttpStatus.OK);
        when(clientTemplate.deleteForEntity(anyString(), anyString(), eq(TemplateOptimizeResp.class)))
            .thenReturn(responseEntity);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            TemplateOptimizeResp result = optimizationTemplateService.deleteOptimization("jiuwen-001");
            assertNotNull(result);
        }
    }

    @Test
    void testDeleteOptimization_Non2xx_Throws() {
        ResponseEntity<TemplateOptimizeResp> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(clientTemplate.deleteForEntity(anyString(), anyString(), eq(TemplateOptimizeResp.class)))
            .thenReturn(responseEntity);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            AgentStudioException exception = assertThrows(AgentStudioException.class,
                () -> optimizationTemplateService.deleteOptimization("jiuwen-001"));
            assertEquals(StudioError.DELETE_OPTIMIZATION_TASK, exception.getErrorCode());
        }
    }

    // ========== getOptimizationProgress ==========

    @Test
    void testGetOptimizationProgress_Success() {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("code", 200);
        jsonBody.put("message", "OK");

        ResponseEntity<JSONObject> responseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(clientTemplate.getForEntity(anyString(), anyString(), eq(JSONObject.class)))
            .thenReturn(responseEntity);

        TemplateOptimizeProgressResp result = optimizationTemplateService.getOptimizationProgress("jiuwen-001", "token");
        assertNotNull(result);
    }

    @Test
    void testGetOptimizationProgress_Non2xx_Throws() {
        ResponseEntity<JSONObject> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(clientTemplate.getForEntity(anyString(), anyString(), eq(JSONObject.class)))
            .thenReturn(responseEntity);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTemplateService.getOptimizationProgress("jiuwen-001", "token"));
        assertEquals(StudioError.GET_OPTIMIZATION_TASK, exception.getErrorCode());
    }

    @Test
    void testGetOptimizationProgress_NullBody_Throws() {
        ResponseEntity<JSONObject> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        when(clientTemplate.getForEntity(anyString(), anyString(), eq(JSONObject.class)))
            .thenReturn(responseEntity);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTemplateService.getOptimizationProgress("jiuwen-001", "token"));
        assertEquals(StudioError.GET_OPTIMIZATION_TASK, exception.getErrorCode());
    }

    @Test
    void testGetOptimizationProgress_ResourceAccessException_Throws() {
        when(clientTemplate.getForEntity(anyString(), anyString(), eq(JSONObject.class)))
            .thenThrow(new ResourceAccessException("Connection refused"));

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTemplateService.getOptimizationProgress("jiuwen-001", "token"));
        assertEquals(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED, exception.getErrorCode());
    }
}
