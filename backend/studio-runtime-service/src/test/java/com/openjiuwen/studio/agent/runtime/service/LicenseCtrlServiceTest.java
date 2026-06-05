/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseInst;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseRecord;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseReportReq;
import com.openjiuwen.studio.agent.runtime.rce.client.AgentManagerClient;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class LicenseCtrlServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentManagerClient managerClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Map<String, List<LicenseRecord>> cacheRecords;

    @InjectMocks
    private LicenseCtrlService licenseCtrlService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(licenseCtrlService, "licenseCtrlEnable", true);
        ReflectionTestUtils.setField(licenseCtrlService, "invocationCtrlEnable", true);
        ReflectionTestUtils.setField(licenseCtrlService, "invocationAttrCode", "not_empty");
        ReflectionTestUtils.setField(licenseCtrlService, "cacheExpireTime", 0);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_reportRecordToManager_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            ResponseEntity<String> responseEntity = new ResponseEntity<>("success", HttpStatus.OK);
            when(managerClient.report(any(LicenseReportReq.class), eq(null), anyString())).thenReturn(responseEntity);

            when(redisClient.delete(anyString())).thenReturn(true);

            // When
            licenseCtrlService.reportRecordToManager();
        });
    }

    @Test
    void test_reportRecordToManager_should_not_throw_exception3() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(managerClient.report(any(LicenseReportReq.class), eq(null), anyString())).thenReturn(null);

            when(redisClient.delete(anyString())).thenReturn(true);

            // When
            licenseCtrlService.reportRecordToManager();
        });
    }

    @Test
    void test_reportRecordToManager_to_manager() {
        Map<String, List<LicenseRecord>> records = new HashMap<>();
        LicenseRecord licenseRecord = new LicenseRecord().setDomainId("d")
            .setAttrCode("a")
            .setIncrementalValue(1)
            .setResourceId("r");

        records.computeIfAbsent("a", key -> new ArrayList<>()).add(licenseRecord);
        licenseRecord = new LicenseRecord().setDomainId("d").setAttrCode("a").setIncrementalValue(2).setResourceId("r");
        records.computeIfAbsent("a", key -> new ArrayList<>()).add(licenseRecord);

        ReflectionTestUtils.setField(licenseCtrlService, "cacheRecords", records);

        licenseCtrlService.reportRecordToManager();
    }

    @Test
    void test_agentInvocationReport_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                LicenseRecord licenseRecord = new LicenseRecord();
                mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class)))
                    .thenReturn(licenseRecord);
                mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn("not_empty");

                LicenseInst inst = new LicenseInst().setMaxValue("1").setCurrentValue("0");
                ResponseEntity<List<LicenseInst>> responseEntity = new ResponseEntity<>(Collections.singletonList(inst),
                    HttpStatus.OK);
                when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                    responseEntity);

                List<LicenseRecord> list = new ArrayList<>();
                LicenseRecord licenseRecord1 = new LicenseRecord();
                list.add(licenseRecord1);
                when(cacheRecords.computeIfAbsent(any(), any())).thenReturn(list);

                when(redisClient.get(anyString())).thenReturn("");

                LicenseRecord record = new LicenseRecord();

                // When
                licenseCtrlService.agentInvocationReport(record, "domainId");
            }
        });
    }

    @Test
    void test_agentInvocationCheck_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn("not_empty");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("not_empty");

            LicenseRecord licenseRecord = new LicenseRecord();
            mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class)))
                .thenReturn(licenseRecord);
            mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn("{}");

            LicenseInst inst = new LicenseInst().setMaxValue("100").setCurrentValue("0");
            ResponseEntity<List<LicenseInst>> responseEntity = new ResponseEntity<>(Collections.singletonList(inst),
                HttpStatus.OK);
            when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                responseEntity);

            when(redisClient.get(anyString())).thenReturn("");

            // When
            try {
                LicenseRecord result = licenseCtrlService.agentInvocationCheck("domainId");
            }catch (AgentStudioException e){
                Assertions.assertEquals(StudioError.AGENT_API_INVOKE_EXCEEDED_QUOTA, e.getErrorCode());
            }
        }
    }

    @Test
    void test_agentInvocationCheck_should_exception() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn("not_empty");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("not_empty");

            LicenseRecord licenseRecord = new LicenseRecord();
            mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class)))
                .thenReturn(licenseRecord);
            mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn("{}");

            LicenseInst inst = new LicenseInst().setMaxValue("100").setCurrentValue("100");
            ResponseEntity<List<LicenseInst>> responseEntity = new ResponseEntity<>(Collections.singletonList(inst),
                HttpStatus.OK);
            when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                responseEntity);

            when(redisClient.get(anyString())).thenReturn("");

            try {
                when(redisClient.get(anyString())).thenReturn("100000");
                licenseCtrlService.agentInvocationCheck("domainId");
                Assertions.fail();
            } catch (AgentStudioException e) {
                Assertions.assertEquals(StudioError.AGENT_API_INVOKE_EXCEEDED_QUOTA, e.getErrorCode());
            }
        }
    }

    @Test
    void test_agentInvocationCheck_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn(null);
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn(null);

                mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class)))
                    .thenReturn(null);
                mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn(null);

                when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                    null);

                when(redisClient.get(anyString())).thenReturn(null);

                // When
                try {
                    LicenseRecord result = licenseCtrlService.agentInvocationCheck("domainId");
                }catch (AgentStudioException e){
                    Assertions.assertEquals(StudioError.AGENT_API_INVOKE_EXCEEDED_QUOTA, e.getErrorCode());
                }
            }
        });
    }

    @Test
    void test_agentInvocationCheck_should_return_not_null5() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn(null);
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn(null);

            mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class))).thenReturn(null);
            mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn(null);

            when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                null);

            when(redisClient.get(anyString())).thenReturn(null);

            // When
            try {
                LicenseRecord result = licenseCtrlService.agentInvocationCheck("domainId");
            }catch (AgentStudioException e){
                Assertions.assertEquals(StudioError.AGENT_API_INVOKE_EXCEEDED_QUOTA, e.getErrorCode());
            }
        }
    }

    @Test
    void test_getLicenseRecord_should_return_not_null() throws Exception {
        try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            LicenseRecord licenseRecord = new LicenseRecord();
            mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class)))
                .thenReturn(licenseRecord);
            mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn("not_empty");

            LicenseInst inst = new LicenseInst().setMaxValue("10").setCurrentValue("0");
            ResponseEntity<List<LicenseInst>> responseEntity = new ResponseEntity<>(Collections.singletonList(inst),
                HttpStatus.OK);
            when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                responseEntity);

            when(redisClient.get(anyString())).thenReturn("");

            // When
            LicenseRecord result = licenseCtrlService.getLicenseRecord("not_empty", "not_empty", "not_empty");

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_getLicenseRecord_should_return_not_null1() throws Exception {
        try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            LicenseRecord licenseRecord = new LicenseRecord();
            mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class)))
                .thenReturn(licenseRecord);
            mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn("");

            LicenseInst inst = new LicenseInst().setMaxValue("10").setCurrentValue("0");
            ResponseEntity<List<LicenseInst>> responseEntity = new ResponseEntity<>(Collections.singletonList(inst),
                HttpStatus.OK);
            when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                responseEntity);

            when(redisClient.get(anyString())).thenReturn("");

            // When
            LicenseRecord result = licenseCtrlService.getLicenseRecord("", "", "");

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_getLicenseRecord_should_return_not_null2() throws Exception {
        try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            LicenseRecord licenseRecord = new LicenseRecord();
            mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class)))
                .thenReturn(licenseRecord);
            mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn("not_empty");

            LicenseInst inst = new LicenseInst().setMaxValue("10").setCurrentValue("0");
            ResponseEntity<List<LicenseInst>> responseEntity = new ResponseEntity<>(Collections.singletonList(inst),
                HttpStatus.OK);
            when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                responseEntity);

            when(redisClient.get(anyString())).thenReturn("not_empty");

            // When
            LicenseRecord result = licenseCtrlService.getLicenseRecord("not_empty", "not_empty", "not_empty");

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_getLicenseRecord_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class)))
                    .thenReturn(null);
                mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn(null);

                when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                    null);

                when(redisClient.get(anyString())).thenReturn(null);

                // When
                LicenseRecord result = licenseCtrlService.getLicenseRecord(null, null, null);
            }
        });
    }

    @Test
    void test_getLicenseRecord_should_return_not_null3() throws Exception {
        try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class))).thenReturn(null);
            mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn(null);

            when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                null);

            when(redisClient.get(anyString())).thenReturn(null);

            // When
            LicenseRecord result = licenseCtrlService.getLicenseRecord(null, null, null);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_licenseRecordReport_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                LicenseRecord licenseRecord = new LicenseRecord();
                mockedStaticJsonUtils.when(() -> JsonUtils.decode(anyString(), eq(LicenseRecord.class)))
                    .thenReturn(licenseRecord);
                mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(LicenseRecord.class))).thenReturn("not_empty");

                LicenseInst inst = new LicenseInst().setMaxValue("10").setCurrentValue("0");
                ResponseEntity<List<LicenseInst>> responseEntity = new ResponseEntity<>(Collections.singletonList(inst),
                    HttpStatus.OK);
                when(managerClient.queryLicense(eq(null), eq(null), anyString(), anyString(), anyString())).thenReturn(
                    responseEntity);

                List<LicenseRecord> list = new ArrayList<>();
                LicenseRecord licenseRecord1 = new LicenseRecord();
                list.add(licenseRecord1);
                when(cacheRecords.computeIfAbsent(any(), any())).thenReturn(list);

                when(redisClient.get(anyString())).thenReturn("not_empty");

                LicenseRecord record = new LicenseRecord();

                // When
                licenseCtrlService.licenseRecordReport("d", "a", 1, 0);
            }
        });
    }
}
