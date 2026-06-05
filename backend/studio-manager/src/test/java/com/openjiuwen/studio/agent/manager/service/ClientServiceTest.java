/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.manager.rce.client.ApigClient;
import com.openjiuwen.studio.agent.manager.rce.client.IamClient;
import com.openjiuwen.studio.agent.manager.rce.service.ClientService;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigApiGroupDetailResp;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainCertificateReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsResp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
class ClientServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApigClient apigClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IamClient iamClient;

    @InjectMocks
    private ClientService clientService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(clientService, "apigClient", apigClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_bindDomains_should_return_not_null1() throws Exception {
        // Given
        when(apigClient.bindDomains(anyString(), anyString(), anyString(), anyString(),
            any(ApigBindDomainsReq.class))).thenReturn(null);

        // When
        ResponseEntity<ApigBindDomainsResp> result = clientService.bindDomains(null, null, null, null, null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_apigApiGroupDetail_should_return_not_null1() throws Exception {
        // Given
        when(apigClient.apigApiGroupDetail(anyString(), anyString(), anyString(), anyString())).thenReturn(null);

        // When
        ResponseEntity<ApigApiGroupDetailResp> result = clientService.apigApiGroupDetail(null, null, null, null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_bindDomainsCertificate_should_return_not_null1() throws Exception {
        // Given
        when(apigClient.bindDomainsCertificate(anyString(), anyString(), anyString(), anyString(), anyString(),
            any(ApigBindDomainCertificateReq.class))).thenReturn(null);

        // When
        ResponseEntity<ApigBindDomainsResp> result = clientService.bindDomainsCertificate(null, null, null, null, null,
            null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_queryIamUserDetail() {
        com.alibaba.fastjson2.JSONObject jsonObject = new JSONObject();
        ResponseEntity<com.alibaba.fastjson2.JSONObject> response = new ResponseEntity<>(jsonObject, HttpStatus.OK);
        when(iamClient.queryIamUserDetail(anyString(), anyString())).thenReturn(response);
        ResponseEntity<com.alibaba.fastjson2.JSONObject> result = clientService.queryIamUserDetail("token", "abc");
        assertNotNull(result);
    }
}
