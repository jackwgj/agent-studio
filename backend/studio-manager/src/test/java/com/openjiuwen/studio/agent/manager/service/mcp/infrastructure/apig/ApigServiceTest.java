/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.infrastructure.apig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.rce.service.ClientService;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigApiGroupDetailResp;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainCertificateReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsResp;
import com.openjiuwen.studio.agent.manager.service.mcp.properties.IamProperties;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
class ApigServiceTest extends BaseTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ClientService clientService;

    @InjectMocks
    private ApigService apigService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(apigService, "apigwInstanceId", "not_empty");
        ReflectionTestUtils.setField(apigService, "hisAccountRotate", true);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testBindDomains2() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("test");

            IamProperties iamProperties = new IamProperties();
            iamProperties.setDomain("test");
            iamProperties.setUser("test");
            iamProperties.setProjectId("test");
            iamProperties.setProjectName("test");
            ReflectionTestUtils.setField(apigService, "iamProperties", iamProperties);
            ApigBindDomainsResp apigBindDomainsResp = new ApigBindDomainsResp();
            apigBindDomainsResp.setId("test1");
            when(clientService.bindDomains(anyString(), anyString(), anyString(), eq("groupId"),
                any(ApigBindDomainsReq.class))).thenReturn(null);
            ApigBindDomainsResp result = apigService.bindDomains("groupId", new ApigBindDomainsReq());
            Assertions.assertNull(result);
        }
    }

    @Test
    void testBindDomains3() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("test");
            IamProperties iamProperties = new IamProperties();
            iamProperties.setDomain("test");
            iamProperties.setUser("test");
            iamProperties.setProjectId("test");
            iamProperties.setProjectName("test");
            ReflectionTestUtils.setField(apigService, "iamProperties", iamProperties);
            ResponseEntity<ApigBindDomainsResp> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(200));
            when(clientService.bindDomains(anyString(), anyString(), anyString(), anyString(),
                any(ApigBindDomainsReq.class))).thenReturn(responseEntity);
            Assertions.assertThrows(Exception.class,
                () -> apigService.bindDomains("groupId", new ApigBindDomainsReq()));
        }
    }

    @Test
    void testBindDomains4() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("test");
            IamProperties iamProperties = new IamProperties();
            iamProperties.setDomain("test");
            iamProperties.setUser("test");
            iamProperties.setProjectId("test");
            iamProperties.setProjectName("test");
            ReflectionTestUtils.setField(apigService, "iamProperties", iamProperties);
            ResponseEntity<ApigBindDomainsResp> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(500));
            when(clientService.bindDomains(anyString(), anyString(), anyString(), anyString(),
                any(ApigBindDomainsReq.class))).thenReturn(responseEntity);
            Assertions.assertThrows(Exception.class,
                () -> apigService.bindDomains("groupId", new ApigBindDomainsReq()));
        }
    }

    @Test
    void testApigApiGroupDetail() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("test");
            IamProperties iamProperties = new IamProperties();
            iamProperties.setDomain("test");
            iamProperties.setUser("test");
            iamProperties.setProjectId("test");
            iamProperties.setProjectName("test");
            ReflectionTestUtils.setField(apigService, "iamProperties", iamProperties);
            ApigApiGroupDetailResp apigBindDomainsResp = new ApigApiGroupDetailResp();
            apigBindDomainsResp.setId("test1");
            ResponseEntity<ApigApiGroupDetailResp> responseEntity = new ResponseEntity<>(apigBindDomainsResp,
                HttpStatusCode.valueOf(200));
            when(clientService.apigApiGroupDetail(anyString(), anyString(), anyString(), anyString())).thenReturn(
                responseEntity);
            ApigApiGroupDetailResp result = apigService.apigApiGroupDetail("groupId");
            Assertions.assertNotNull(result);
        }
    }

    @Test
    void testApigApiGroupDetail2() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("test");
            IamProperties iamProperties = new IamProperties();
            iamProperties.setDomain("test");
            iamProperties.setUser("test");
            iamProperties.setProjectId("test");
            iamProperties.setProjectName("test");
            ReflectionTestUtils.setField(apigService, "iamProperties", iamProperties);
            ApigApiGroupDetailResp apigBindDomainsResp = new ApigApiGroupDetailResp();
            apigBindDomainsResp.setId("test1");
            when(clientService.apigApiGroupDetail(anyString(), anyString(), anyString(), anyString())).thenReturn(null);
            ApigApiGroupDetailResp result = apigService.apigApiGroupDetail("groupId");
            Assertions.assertNull(result);

        }
    }

    @Test
    void testBindDomainsCertificate() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("test");
            IamProperties iamProperties = new IamProperties();
            iamProperties.setDomain("test");
            iamProperties.setUser("test");
            iamProperties.setProjectId("test");
            iamProperties.setProjectName("test");
            ReflectionTestUtils.setField(apigService, "iamProperties", iamProperties);
            when(clientService.bindDomainsCertificate(anyString(), anyString(), anyString(), anyString(), anyString(),
                any(ApigBindDomainCertificateReq.class))).thenReturn(null);
            apigService.bindDomainsCertificate("groupId", "domainId", new ApigBindDomainCertificateReq());
        }
    }
}
