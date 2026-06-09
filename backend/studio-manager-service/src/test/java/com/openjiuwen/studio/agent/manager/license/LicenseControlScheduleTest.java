/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.license;

import static com.openjiuwen.studio.agent.manager.license.LicenseControlSchedule.RUNTIME_CPU_NUMS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.entity.TokenResponse;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.service.IamAuthService;
import com.openjiuwen.studio.agent.common.utils.DateUtils;
import com.openjiuwen.studio.agent.common.utils.UuidUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.license.models.LcsResourceDto;
import com.openjiuwen.studio.agent.manager.license.models.LcsResourceDtoWrapper;
import com.openjiuwen.studio.agent.manager.license.models.LcsResourceInfo;
import com.openjiuwen.studio.agent.manager.rce.client.ItepClient;
import com.openjiuwen.studio.common.service.entity.LicenseEntity;
import com.openjiuwen.studio.common.service.repository.LicenseRepository;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class LicenseControlScheduleTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private List<String> modeLicenses =
        new ArrayList<>(Arrays.asList("VAPENTSV001", "VAPENTSV002", "VAPENT001", "VAPENT002"));

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private K8sService k8sService;

    @Mock
    private ItepClient itepClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IamAuthService iamAuthService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LicenseRepository licenseRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @InjectMocks
    private LicenseControlSchedule licenseControlSchedule;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(licenseControlSchedule, "modeLicenses", modeLicenses);
        ReflectionTestUtils.setField(licenseControlSchedule, "k8sService", k8sService);
        ReflectionTestUtils.setField(licenseControlSchedule, "itepClient", itepClient);
        ReflectionTestUtils.setField(licenseControlSchedule, "iamAuthService", iamAuthService);
        ReflectionTestUtils.setField(licenseControlSchedule, "opUserId", "not_empty");
        ReflectionTestUtils.setField(licenseControlSchedule, "ak", "not_empty");
        ReflectionTestUtils.setField(licenseControlSchedule, "sk", "not_empty");
        ReflectionTestUtils.setField(licenseControlSchedule, "projectName", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_syncLicenseTask_should_not_throw_exception() {
        assertDoesNotThrow(() -> {
            try (MockedStatic<UuidUtils> mockedStaticUuidUtils = mockStatic(UuidUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<DateUtils> mockedStaticDateUtils = mockStatic(DateUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticUuidUtils.when(UuidUtils::getUUID).thenReturn("8276b3ba3b45451f831adaaa76062e97");

                Timestamp timestamp = new Timestamp(0L);
                mockedStaticDateUtils.when(DateUtils::getNow).thenReturn(timestamp);

                LcsResourceDto lcsResourceDto = new LcsResourceDto();
                ResponseEntity<LcsResourceDto> responseEntity1 = ResponseEntity.ok(lcsResourceDto);
                when(itepClient.deleteResource(anyString(), eq(CommonConstant.APPLICATION_JSON),
                    any(LcsResourceDto.class))).thenReturn(responseEntity1);

                // 给 header 填上 token
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Subject-Token", "mock-token-value");
                TokenResponse emptyBody = new TokenResponse();
                ResponseEntity<TokenResponse> tokenResponseResponseEntity =
                    new ResponseEntity<>(emptyBody, headers, HttpStatus.OK);
                when(iamAuthService.getProjectTokenByAkSk(any(), any(), any(), any())).thenReturn("mock-token-value");

                LcsResourceDtoWrapper lcsResourceDtoWrapper = getLcsResourceDtoWrapper("VAPENTSV002", 2);
                ResponseEntity<LcsResourceDtoWrapper> responseEntity = ResponseEntity.ok(lcsResourceDtoWrapper);
                when(itepClient.getResourceLicneseInfo(anyString(), anyString())).thenReturn(responseEntity);

                LcsResourceDto lcsResourceDto2 = new LcsResourceDto();
                ResponseEntity<LcsResourceDto> responseEntity2 = ResponseEntity.ok(lcsResourceDto2);
                when(
                    itepClient.addResource(anyString(), eq(CommonConstant.APPLICATION_JSON), any(LcsResourceDto.class)))
                    .thenReturn(responseEntity2);

                when(modeLicenses.contains(anyString())).thenReturn(true);

                List<LicenseEntity> licenseEntities = new ArrayList<>();
                when(licenseRepository.selectByResourceId(anyString(), anyString())).thenReturn(licenseEntities);

                when(k8sService.getAgentResourceCpu()).thenReturn(0.0d);

                when(redisClient.get(eq(RUNTIME_CPU_NUMS))).thenReturn("20");

                // When
                licenseControlSchedule.syncLicenseTask();
            }
        });
    }

    @NotNull
    private static LcsResourceDtoWrapper getLcsResourceDtoWrapper(String itemName, int itemUsed) {
        LcsResourceDtoWrapper lcsResourceDtoWrapper = new LcsResourceDtoWrapper();
        lcsResourceDtoWrapper.setCount(1);
        LcsResourceInfo resourceInfo = new LcsResourceInfo();
        resourceInfo.setItemName(itemName);
        resourceInfo.setItemTotalNum(20);
        resourceInfo.setItemUsedNum(itemUsed);
        List<LcsResourceInfo> list = new ArrayList<>();
        list.add(resourceInfo);
        lcsResourceDtoWrapper.setResources(list);
        return lcsResourceDtoWrapper;
    }

    @Test
    void test_getTokenByPassword_should_equal_result() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Subject-Token", "mock-token-value");
        TokenResponse emptyBody = new TokenResponse();
        ResponseEntity<TokenResponse> tokenResponseResponseEntity =
            new ResponseEntity<>(emptyBody, headers, HttpStatus.OK);
        when(iamAuthService.getProjectTokenByAkSk(any(), any(), any(), any())).thenReturn("mock-token-value");

        // When
        String result = licenseControlSchedule.getTokenByPassword();

        // Then
        assertEquals("mock-token-value", result);
    }

    @Test
    void test_syncLicenseTask_should_not_throw_exception1() {
        assertDoesNotThrow(() -> {
            try (MockedStatic<UuidUtils> mockedStaticUuidUtils = mockStatic(UuidUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<DateUtils> mockedStaticDateUtils = mockStatic(DateUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticUuidUtils.when(UuidUtils::getUUID).thenReturn("not_empty");

                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Subject-Token", "mock-token-value");
                TokenResponse emptyBody = new TokenResponse();
                ResponseEntity<TokenResponse> tokenResponseResponseEntity =
                    new ResponseEntity<>(emptyBody, headers, HttpStatus.OK);
                when(iamAuthService.getProjectTokenByAkSk(any(), any(), any(), any())).thenReturn("mock-token-value");

                Timestamp timestamp = new Timestamp(0L);
                mockedStaticDateUtils.when(DateUtils::getNow).thenReturn(timestamp);

                List<LicenseEntity> licenseEntities = new ArrayList<>();
                when(licenseRepository.selectByResourceId(anyString(), anyString())).thenReturn(licenseEntities);

                when(modeLicenses.contains(anyString())).thenReturn(true);

                when(k8sService.getAgentResourceCpu()).thenReturn(0.0d);

                when(redisClient.get(eq(RUNTIME_CPU_NUMS))).thenReturn("2");
                when(redisClient.exists(eq(RUNTIME_CPU_NUMS))).thenReturn(true);

                LcsResourceDto lcsResourceDto1 = new LcsResourceDto();
                ResponseEntity<LcsResourceDto> responseEntity = ResponseEntity.ok(lcsResourceDto1);
                when(itepClient.deleteResource(anyString(), eq(CommonConstant.APPLICATION_JSON),
                    any(LcsResourceDto.class))).thenReturn(responseEntity);

                LcsResourceDto lcsResourceDto2 = new LcsResourceDto();
                ResponseEntity<LcsResourceDto> responseEntity2 = ResponseEntity.ok(lcsResourceDto2);
                when(
                    itepClient.addResource(anyString(), eq(CommonConstant.APPLICATION_JSON), any(LcsResourceDto.class)))
                    .thenReturn(responseEntity2);

                LcsResourceDtoWrapper lcsResourceDtoWrapper = getLcsResourceDtoWrapper("VAPENTSV001", 2);
                ResponseEntity<LcsResourceDtoWrapper> responseEntity3 = ResponseEntity.ok(lcsResourceDtoWrapper);
                when(itepClient.getResourceLicneseInfo(anyString(), anyString())).thenReturn(responseEntity3);

                // When
                licenseControlSchedule.syncLicenseTask();
            }
        });
    }

    @Test
    void test_syncLicenseTask_should_not_throw_exception2() {
        assertDoesNotThrow(() -> {
            try (MockedStatic<UuidUtils> mockedStaticUuidUtils = mockStatic(UuidUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<DateUtils> mockedStaticDateUtils = mockStatic(DateUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticUuidUtils.when(UuidUtils::getUUID).thenReturn("not_empty");

                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Subject-Token", "mock-token-value");
                TokenResponse emptyBody = new TokenResponse();
                ResponseEntity<TokenResponse> tokenResponseResponseEntity =
                    new ResponseEntity<>(emptyBody, headers, HttpStatus.OK);
                when(iamAuthService.getProjectTokenByAkSk(any(), any(), any(), any())).thenReturn("mock-token-value");

                Timestamp timestamp = new Timestamp(0L);
                mockedStaticDateUtils.when(DateUtils::getNow).thenReturn(timestamp);

                List<LicenseEntity> licenseEntities = new ArrayList<>();
                when(licenseRepository.selectByResourceId(anyString(), anyString())).thenReturn(licenseEntities);

                when(modeLicenses.contains(anyString())).thenReturn(true);

                when(k8sService.getAgentResourceCpu()).thenReturn(0.0d);

                when(redisClient.get(eq(RUNTIME_CPU_NUMS))).thenReturn("90");
                when(redisClient.exists(eq(RUNTIME_CPU_NUMS))).thenReturn(true);

                LcsResourceDto lcsResourceDto1 = new LcsResourceDto();
                ResponseEntity<LcsResourceDto> responseEntity = ResponseEntity.ok(lcsResourceDto1);
                when(itepClient.deleteResource(anyString(), eq(CommonConstant.APPLICATION_JSON),
                    any(LcsResourceDto.class))).thenReturn(responseEntity);

                LcsResourceDto lcsResourceDto2 = new LcsResourceDto();
                ResponseEntity<LcsResourceDto> responseEntity2 = ResponseEntity.ok(lcsResourceDto2);
                when(
                    itepClient.addResource(anyString(), eq(CommonConstant.APPLICATION_JSON), any(LcsResourceDto.class)))
                    .thenReturn(responseEntity2);

                LcsResourceDtoWrapper lcsResourceDtoWrapper = getLcsResourceDtoWrapper("VAPENTSV001", 0);
                ResponseEntity<LcsResourceDtoWrapper> responseEntity3 = ResponseEntity.ok(lcsResourceDtoWrapper);
                when(itepClient.getResourceLicneseInfo(anyString(), anyString())).thenReturn(responseEntity3);

                // When
                licenseControlSchedule.syncLicenseTask();
            }
        });
    }

    @Test
    void test_syncLicenseTask_should_not_throw_exception3() {
        assertDoesNotThrow(() -> {
            try (MockedStatic<UuidUtils> mockedStaticUuidUtils = mockStatic(UuidUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<DateUtils> mockedStaticDateUtils = mockStatic(DateUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticUuidUtils.when(UuidUtils::getUUID).thenReturn("not_empty");

                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Subject-Token", "mock-token-value");
                TokenResponse emptyBody = new TokenResponse();
                ResponseEntity<TokenResponse> tokenResponseResponseEntity =
                    new ResponseEntity<>(emptyBody, headers, HttpStatus.OK);
                when(iamAuthService.getProjectTokenByAkSk(any(), any(), any(), any())).thenReturn("mock-token-value");

                Timestamp timestamp = new Timestamp(0L);
                mockedStaticDateUtils.when(DateUtils::getNow).thenReturn(timestamp);

                List<LicenseEntity> licenseEntities = new ArrayList<>();
                when(licenseRepository.selectByResourceId(anyString(), anyString())).thenReturn(licenseEntities);

                when(modeLicenses.contains(anyString())).thenReturn(true);

                when(k8sService.getAgentResourceCpu()).thenReturn(0.0d);

                when(redisClient.get(eq(RUNTIME_CPU_NUMS))).thenReturn("90");
                when(redisClient.exists(eq(RUNTIME_CPU_NUMS))).thenReturn(true);

                LcsResourceDto lcsResourceDto1 = new LcsResourceDto();
                ResponseEntity<LcsResourceDto> responseEntity = ResponseEntity.ok(lcsResourceDto1);
                when(itepClient.deleteResource(anyString(), eq(CommonConstant.APPLICATION_JSON),
                    any(LcsResourceDto.class))).thenReturn(responseEntity);

                LcsResourceDto lcsResourceDto2 = new LcsResourceDto();
                ResponseEntity<LcsResourceDto> responseEntity2 = ResponseEntity.ok(lcsResourceDto2);
                when(
                    itepClient.addResource(anyString(), eq(CommonConstant.APPLICATION_JSON), any(LcsResourceDto.class)))
                    .thenReturn(responseEntity2);

                LcsResourceDtoWrapper lcsResourceDtoWrapper = getLcsResourceDtoWrapper("VAPENTSV001", 2);
                ResponseEntity<LcsResourceDtoWrapper> responseEntity3 = ResponseEntity.ok(lcsResourceDtoWrapper);
                when(itepClient.getResourceLicneseInfo(anyString(), anyString())).thenReturn(responseEntity3);

                // When
                licenseControlSchedule.syncLicenseTask();
            }
        });
    }

    @Test
    void test_syncLicenseTask_should_throw_exception2() {
        assertThrows(Exception.class, () -> {
            try (MockedStatic<UuidUtils> mockedStaticUuidUtils = mockStatic(UuidUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<DateUtils> mockedStaticDateUtils = mockStatic(DateUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticUuidUtils.when(UuidUtils::getUUID).thenReturn("t32thgsyah");

                mockedStaticDateUtils.when(DateUtils::getNow).thenReturn(null);

                when(licenseRepository.selectByResourceId(anyString(), anyString())).thenReturn(null);

                when(modeLicenses.contains(anyString())).thenReturn(true);

                when(k8sService.getAgentResourceCpu()).thenReturn(0.0d);

                when(redisClient.get(eq(RUNTIME_CPU_NUMS))).thenReturn(null);
                when(redisClient.exists(eq(RUNTIME_CPU_NUMS))).thenReturn(true);

                when(itepClient.deleteResource(anyString(), eq(CommonConstant.APPLICATION_JSON),
                    any(LcsResourceDto.class))).thenReturn(null);
                when(
                    itepClient.addResource(anyString(), eq(CommonConstant.APPLICATION_JSON), any(LcsResourceDto.class)))
                    .thenReturn(null);
                when(itepClient.getResourceLicneseInfo(anyString(), eq(CommonConstant.APPLICATION_JSON)))
                    .thenReturn(null);

                when(iamAuthService.getProjectTokenByAkSk(any(), any(), any(), any())).thenReturn("mock-token-value");

                // When
                licenseControlSchedule.syncLicenseTask();
            }
        });
    }

    @Test
    void test_getTokenByPassword_should_equal_result1() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Subject-Token", "mock-token-value");
        TokenResponse emptyBody = new TokenResponse();
        ResponseEntity<TokenResponse> tokenResponseResponseEntity =
            new ResponseEntity<>(emptyBody, headers, HttpStatus.OK);
        when(iamAuthService.getProjectTokenByAkSk(any(), any(), any(), any())).thenReturn("mock-token-value");

        // When
        String result = licenseControlSchedule.getTokenByPassword();

        // Then
        assertEquals("mock-token-value", result);
    }

    @Test
    void test_getTokenByPassword_should_equal_result2() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Subject-Token", "token");
        TokenResponse emptyBody = new TokenResponse();
        ResponseEntity<TokenResponse> tokenResponseResponseEntity =
            new ResponseEntity<>(emptyBody, headers, HttpStatus.OK);
        when(iamAuthService.getProjectTokenByAkSk(any(), any(), any(), any())).thenReturn("token");

        // When
        String result = licenseControlSchedule.getTokenByPassword();

        // Then
        assertEquals("token", result);
    }

    @Test
    void test_updateLicenseTotalNum_should_not_throw_exception() {
        assertDoesNotThrow(() -> {
            try (MockedStatic<UuidUtils> mockedStaticUuidUtils = mockStatic(UuidUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<DateUtils> mockedStaticDateUtils = mockStatic(DateUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticUuidUtils.when(UuidUtils::getUUID).thenReturn("not_empty");

                Timestamp timestamp = new Timestamp(0L);
                mockedStaticDateUtils.when(DateUtils::getNow).thenReturn(timestamp);

                List<LicenseEntity> licenseEntities = new ArrayList<>();
                when(licenseRepository.selectByResourceId(anyString(), anyString())).thenReturn(licenseEntities);

                LcsResourceInfo lcsResourceInfo = LcsResourceInfo.builder().itemUsedNum(0).itemTotalNum(0).build();

                // When
                licenseControlSchedule.updateLicenseTotalNum(lcsResourceInfo, "not_empty");
            }
        });
    }
}
