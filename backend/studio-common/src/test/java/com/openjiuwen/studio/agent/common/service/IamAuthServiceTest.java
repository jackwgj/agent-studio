/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.entity.AuthRequestWrapper;
import com.openjiuwen.studio.agent.common.entity.TokenResponse;
import com.openjiuwen.studio.agent.common.rce.client.IamClient;

import com.openjiuwen.studio.agent.common.service.IamAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

/**
 * IamAuthService 测试类
 *
 */
@ExtendWith(MockitoExtension.class)
class IamAuthServiceTest {

    @Mock
    private IamClient iamClient;

    @InjectMocks
    private IamAuthService iamAuthService;

    private static final String TEST_PASSWORD = "testPassword123";

    private static final String TEST_USER_NAME = "testUser";

    private static final String TEST_USER_DOMAIN = "testDomain";

    private static final String TEST_TOKEN = "mock-subject-token";

    @BeforeEach
    void setUp() {
        // 使用Spring的ReflectionTestUtils设置私有字段值
        ReflectionTestUtils.setField(iamAuthService, "userName", "testUser");
        ReflectionTestUtils.setField(iamAuthService, "userDomain", "testDomain");
        ReflectionTestUtils.setField(iamAuthService, "projectName", "testProject");
        ReflectionTestUtils.setField(iamAuthService, "agencyName", "testAgency");
    }

    /**
     * 测试 authenticateWithPasswordDomain 方法的成功场景
     */
    @Test
    void testAuthenticateWithPasswordDomain_Success() throws JsonProcessingException {
        // 准备模拟响应
        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.Header.X_SUBJECT_TOKEN, TEST_TOKEN);

        TokenResponse tokenResponse = TokenResponse.builder()
            .token(TokenResponse.Token.builder()
                .expiresAt("2025-12-31T23:59:59.000Z")
                .methods(Collections.singletonList("password"))
                .build())
            .build();

        ResponseEntity<TokenResponse> responseEntity = new ResponseEntity<>(tokenResponse, headers, HttpStatus.OK);

        // Mock IamClient 的行为
        when(iamClient.getTokenByPassword(any(AuthRequestWrapper.class))).thenReturn(responseEntity);

        // 执行测试
        String result = iamAuthService.authenticateWithPasswordDomain(TEST_PASSWORD);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_TOKEN, result);

        // 验证调用
        verify(iamClient, times(1)).getTokenByPassword(any(AuthRequestWrapper.class));
    }

    /**
     * 测试 authenticateWithPasswordDomain 方法在密码为空时的情况
     */
    @Test
    void testAuthenticateWithPasswordDomain_EmptyPassword() throws JsonProcessingException {
        // 准备模拟响应
        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.Header.X_SUBJECT_TOKEN, TEST_TOKEN);

        TokenResponse tokenResponse = TokenResponse.builder()
            .token(TokenResponse.Token.builder()
                .expiresAt("2025-12-31T23:59:59.000Z")
                .methods(Collections.singletonList("password"))
                .build())
            .build();

        ResponseEntity<TokenResponse> responseEntity = new ResponseEntity<>(tokenResponse, headers, HttpStatus.OK);

        // Mock IamClient 的行为
        when(iamClient.getTokenByPassword(any(AuthRequestWrapper.class))).thenReturn(responseEntity);

        // 执行测试
        String result = iamAuthService.authenticateWithPasswordDomain("");

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_TOKEN, result);

        // 验证调用
        verify(iamClient, times(1)).getTokenByPassword(any(AuthRequestWrapper.class));
    }

    /**
     * 测试 authenticateWithPasswordDomain 方法在特殊字符密码时的情况
     */
    @Test
    void testAuthenticateWithPasswordDomain_SpecialCharactersPassword() throws JsonProcessingException {
        String specialPassword = "P@ssw0rd!@#$%^&*()";

        // 准备模拟响应
        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.Header.X_SUBJECT_TOKEN, TEST_TOKEN);

        TokenResponse tokenResponse = TokenResponse.builder()
            .token(TokenResponse.Token.builder()
                .expiresAt("2025-12-31T23:59:59.000Z")
                .methods(Collections.singletonList("password"))
                .build())
            .build();

        ResponseEntity<TokenResponse> responseEntity = new ResponseEntity<>(tokenResponse, headers, HttpStatus.OK);

        // Mock IamClient 的行为
        when(iamClient.getTokenByPassword(any(AuthRequestWrapper.class))).thenReturn(responseEntity);

        // 执行测试
        String result = iamAuthService.authenticateWithPasswordDomain(specialPassword);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_TOKEN, result);

        // 验证调用
        verify(iamClient, times(1)).getTokenByPassword(any(AuthRequestWrapper.class));
    }

    /**
     * 测试生成的 AuthRequestWrapper 结构是否正确
     */
    @Test
    void testAuthenticateWithPasswordDomain_RequestStructure() throws JsonProcessingException {
        // 准备模拟响应
        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.Header.X_SUBJECT_TOKEN, TEST_TOKEN);

        TokenResponse tokenResponse = TokenResponse.builder()
            .token(TokenResponse.Token.builder()
                .expiresAt("2025-12-31T23:59:59.000Z")
                .methods(Collections.singletonList("password"))
                .build())
            .build();

        ResponseEntity<TokenResponse> responseEntity = new ResponseEntity<>(tokenResponse, headers, HttpStatus.OK);

        // Mock IamClient 的行为
        when(iamClient.getTokenByPassword(any(AuthRequestWrapper.class))).thenReturn(responseEntity);

        // 执行测试
        String result = iamAuthService.authenticateWithPasswordDomain(TEST_PASSWORD);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_TOKEN, result);

        // 验证调用参数（通过 ArgumentCaptor 来验证具体的请求结构）
        verify(iamClient, times(1)).getTokenByPassword(argThat(request -> {
            // 验证请求的基本结构
            assertNotNull(request);
            assertNotNull(request.getAuth());
            assertNotNull(request.getAuth().getIdentity());
            assertNotNull(request.getAuth().getScope());

            // 验证认证方法
            assertEquals(1, request.getAuth().getIdentity().getMethods().size());
            assertEquals("password", request.getAuth().getIdentity().getMethods().get(0));

            // 验证密码凭据
            assertNotNull(request.getAuth().getIdentity().getPassword());
            assertNotNull(request.getAuth().getIdentity().getPassword().getUser());
            assertEquals(TEST_USER_NAME, request.getAuth().getIdentity().getPassword().getUser().getName());
            assertEquals(TEST_PASSWORD, request.getAuth().getIdentity().getPassword().getUser().getPassword());
            assertNotNull(request.getAuth().getIdentity().getPassword().getUser().getDomain());
            assertEquals(TEST_USER_DOMAIN,
                request.getAuth().getIdentity().getPassword().getUser().getDomain().getName());

            // 验证范围设置
            assertNotNull(request.getAuth().getScope().getDomain());
            assertEquals(TEST_USER_DOMAIN, request.getAuth().getScope().getDomain().getName());

            return true;
        }));
    }

    /**
     * 测试 IamClient 抛出异常时的处理
     */
    @Test
    void testAuthenticateWithPasswordDomain_IamClientException() throws JsonProcessingException {
        // Mock IamClient 抛出异常
        when(iamClient.getTokenByPassword(any(AuthRequestWrapper.class)))
            .thenThrow(new RuntimeException("IAM service unavailable"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            iamAuthService.authenticateWithPasswordDomain(TEST_PASSWORD);
        });

        // 验证调用
        verify(iamClient, times(1)).getTokenByPassword(any(AuthRequestWrapper.class));
    }

    /**
     * 测试响应中没有 X-Subject-Token 头的情况
     */
    @Test
    void testAuthenticateWithPasswordDomain_MissingTokenHeader() throws JsonProcessingException {
        // 准备没有 X-Subject-Token 头的响应
        TokenResponse tokenResponse = TokenResponse.builder()
            .token(TokenResponse.Token.builder()
                .expiresAt("2025-12-31T23:59:59.000Z")
                .methods(Collections.singletonList("password"))
                .build())
            .build();

        ResponseEntity<TokenResponse> responseEntity =
            new ResponseEntity<>(tokenResponse, new HttpHeaders(), HttpStatus.OK);

        // Mock IamClient 的行为
        when(iamClient.getTokenByPassword(any(AuthRequestWrapper.class))).thenReturn(responseEntity);

        // 执行测试
        String result = iamAuthService.authenticateWithPasswordDomain(TEST_PASSWORD);

        // 验证结果
        assertNull(result);

        // 验证调用
        verify(iamClient, times(1)).getTokenByPassword(any(AuthRequestWrapper.class));
    }
}
