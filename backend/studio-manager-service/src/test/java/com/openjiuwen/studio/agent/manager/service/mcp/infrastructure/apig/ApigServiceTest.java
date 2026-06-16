package com.openjiuwen.studio.agent.manager.service.mcp.infrastructure.apig;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.rce.service.ClientService;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigApiGroupDetailResp;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainCertificateReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsResp;
import com.openjiuwen.studio.agent.manager.service.mcp.properties.IamProperties;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;
import com.openjiuwen.studio.agent.manager.utils.McpServiceExceptionUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApigServiceTest {

    @Mock
    private ClientService clientService;

    @Mock
    private IamProperties iamProperties;

    @Mock
    private IamServiceUtils iamServiceUtils;

    @InjectMocks
    private ApigService apigService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apigService, "apigwInstanceId", "test-instance-id");
        when(iamProperties.getProjectId()).thenReturn("test-project-id");
    }

    @Test
    void testBindDomains_Success() {
        ApigBindDomainsResp resp = new ApigBindDomainsResp();
        resp.setId("domain-id");
        ResponseEntity<ApigBindDomainsResp> responseEntity = ResponseEntity.ok(resp);
        ApigBindDomainsReq req = new ApigBindDomainsReq();

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");

            when(clientService.bindDomains(eq("test-token"), eq("test-project-id"),
                eq("test-instance-id"), eq("group-1"), any(ApigBindDomainsReq.class)))
                .thenReturn(responseEntity);

            ApigBindDomainsResp result = apigService.bindDomains("group-1", req);
            assertNotNull(result);
            assertEquals("domain-id", result.getId());
        }
    }

    @Test
    void testBindDomains_NullResponse_ReturnsNull() {
        ApigBindDomainsReq req = new ApigBindDomainsReq();

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");

            when(clientService.bindDomains(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(null);

            ApigBindDomainsResp result = apigService.bindDomains("group-1", req);
            assertNull(result);
        }
    }

    @Test
    void testBindDomains_EmptyBody_ThrowsException() {
        ApigBindDomainsReq req = new ApigBindDomainsReq();
        ResponseEntity<ApigBindDomainsResp> responseEntity = ResponseEntity.ok(null);

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<McpServiceExceptionUtils> exceptionMock = mockStatic(McpServiceExceptionUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            exceptionMock.when(() -> McpServiceExceptionUtils.throwRemoteCallException(anyString(), anyString()))
                .thenThrow(new AgentStudioException(null));

            when(clientService.bindDomains(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(responseEntity);

            assertThrows(AgentStudioException.class, () -> apigService.bindDomains("group-1", req));
        }
    }

    @Test
    void testBindDomains_Non2xxStatus_ThrowsException() {
        ApigBindDomainsReq req = new ApigBindDomainsReq();
        ResponseEntity<ApigBindDomainsResp> responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<McpServiceExceptionUtils> exceptionMock = mockStatic(McpServiceExceptionUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            exceptionMock.when(() -> McpServiceExceptionUtils.throwRemoteCallException(anyString(), anyString()))
                .thenThrow(new AgentStudioException(null));

            when(clientService.bindDomains(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(responseEntity);

            assertThrows(AgentStudioException.class, () -> apigService.bindDomains("group-1", req));
        }
    }

    @Test
    void testApigApiGroupDetail_Success() {
        ApigApiGroupDetailResp resp = new ApigApiGroupDetailResp();
        resp.setId("group-detail-id");
        ResponseEntity<ApigApiGroupDetailResp> responseEntity = ResponseEntity.ok(resp);

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");

            when(clientService.apigApiGroupDetail(eq("test-token"), eq("test-project-id"),
                eq("test-instance-id"), eq("group-1")))
                .thenReturn(responseEntity);

            ApigApiGroupDetailResp result = apigService.apigApiGroupDetail("group-1");
            assertNotNull(result);
            assertEquals("group-detail-id", result.getId());
        }
    }

    @Test
    void testApigApiGroupDetail_NullResponse_ReturnsNull() {
        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");

            when(clientService.apigApiGroupDetail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

            ApigApiGroupDetailResp result = apigService.apigApiGroupDetail("group-1");
            assertNull(result);
        }
    }

    @Test
    void testApigApiGroupDetail_EmptyBody_ThrowsException() {
        ResponseEntity<ApigApiGroupDetailResp> responseEntity = ResponseEntity.ok(null);

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<McpServiceExceptionUtils> exceptionMock = mockStatic(McpServiceExceptionUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            exceptionMock.when(() -> McpServiceExceptionUtils.throwRemoteCallException(anyString(), anyString()))
                .thenThrow(new AgentStudioException(null));

            when(clientService.apigApiGroupDetail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(responseEntity);

            assertThrows(AgentStudioException.class, () -> apigService.apigApiGroupDetail("group-1"));
        }
    }

    @Test
    void testApigApiGroupDetail_Non2xxStatus_ThrowsException() {
        ResponseEntity<ApigApiGroupDetailResp> responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class);
             MockedStatic<McpServiceExceptionUtils> exceptionMock = mockStatic(McpServiceExceptionUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            exceptionMock.when(() -> McpServiceExceptionUtils.throwRemoteCallException(anyString(), anyString()))
                .thenThrow(new AgentStudioException(null));

            when(clientService.apigApiGroupDetail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(responseEntity);

            assertThrows(AgentStudioException.class, () -> apigService.apigApiGroupDetail("group-1"));
        }
    }

    @Test
    void testBindDomainsCertificate_Success() {
        ApigBindDomainCertificateReq req = new ApigBindDomainCertificateReq();
        req.setCertificateIds(List.of("cert-1", "cert-2"));

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");

            when(clientService.bindDomainsCertificate(eq("test-token"), eq("test-project-id"),
                eq("test-instance-id"), eq("group-1"), eq("domain-1"), any(ApigBindDomainCertificateReq.class)))
                .thenReturn(ResponseEntity.ok().build());

            apigService.bindDomainsCertificate("group-1", "domain-1", req);
        }
    }
}
