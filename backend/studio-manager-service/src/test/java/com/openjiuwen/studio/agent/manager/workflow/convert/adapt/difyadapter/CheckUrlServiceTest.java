/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class CheckUrlServiceTest {

    @InjectMocks
    private CheckUrlService checkUrlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(checkUrlService, "enableUrlCheck", true);
    }

    @Test
    void emptyHostBlackList_returnsNormally() {
        checkUrlService.checkSwaggerUrlValid("http://example.com/path", "");
    }

    @Test
    void enableUrlCheckFalse_returnsNormally() {
        ReflectionTestUtils.setField(checkUrlService, "enableUrlCheck", false);
        checkUrlService.checkSwaggerUrlValid("http://evil.com/path", "evil.com");
    }

    @Test
    void malformedUrl_throwsINVALID_URL() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> checkUrlService.checkSwaggerUrlValid("not-a-valid-url", "evil.com"));
        assertEquals(StudioError.INVALID_URL, exception.getErrorCode());
    }

    @Test
    void hostInBlacklist_throwsWORKFLOW_ADDRESS_RESTRICTED() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> checkUrlService.checkSwaggerUrlValid("http://evil.com/path", "evil.com"));
        assertEquals(StudioError.WORKFLOW_ADDRESS_RESTRICTED, exception.getErrorCode());
    }

    @Test
    void ipInBlacklist_throwsWORKFLOW_ADDRESS_RESTRICTED() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> checkUrlService.checkSwaggerUrlValid("http://10.0.0.1/path", "10.0.0.1"));
        assertEquals(StudioError.WORKFLOW_ADDRESS_RESTRICTED, exception.getErrorCode());
    }

    @Test
    void ipSubnetMatch_throwsWORKFLOW_ADDRESS_RESTRICTED() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> checkUrlService.checkSwaggerUrlValid("http://10.0.0.5/path", "10.0.0.0/24"));
        assertEquals(StudioError.WORKFLOW_ADDRESS_RESTRICTED, exception.getErrorCode());
    }

    @Test
    void hostStringContainsBlacklistIp_throwsWORKFLOW_ADDRESS_RESTRICTED() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> checkUrlService.checkSwaggerUrlValid("http://10.0.0.15/path", "10.0.0.1"));
        assertEquals(StudioError.WORKFLOW_ADDRESS_RESTRICTED, exception.getErrorCode());
    }

    @Test
    void validUrlNotInBlacklist_returnsNormally() {
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
            InetAddress mockAddress = mock(InetAddress.class);
            inetAddressMock.when(() -> InetAddress.getAllByName("safe.com"))
                .thenReturn(new InetAddress[]{mockAddress});
            org.mockito.Mockito.when(mockAddress.getHostAddress()).thenReturn("203.0.113.1");

            checkUrlService.checkSwaggerUrlValid("http://safe.com/path", "evil.com,192.168.1.0/24");
        }
    }

    @Test
    void unknownHost_returnsNormally() {
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
            inetAddressMock.when(() -> InetAddress.getAllByName("unknown.invalid"))
                .thenThrow(new UnknownHostException("unknown.invalid"));

            checkUrlService.checkSwaggerUrlValid("http://unknown.invalid/path", "evil.com");
        }
    }

    @Test
    void dnsResolvesToBlacklistedIp_throwsWORKFLOW_ADDRESS_RESTRICTED() {
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
            InetAddress mockAddress = mock(InetAddress.class);
            inetAddressMock.when(() -> InetAddress.getAllByName("safe.com"))
                .thenReturn(new InetAddress[]{mockAddress});
            org.mockito.Mockito.when(mockAddress.getHostAddress()).thenReturn("10.0.0.1");

            AgentStudioException exception = assertThrows(AgentStudioException.class,
                () -> checkUrlService.checkSwaggerUrlValid("http://safe.com/path", "10.0.0.1"));
            assertEquals(StudioError.WORKFLOW_ADDRESS_RESTRICTED, exception.getErrorCode());
        }
    }

    @Test
    void dnsResolvesToIpInSubnet_throwsWORKFLOW_ADDRESS_RESTRICTED() {
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
            InetAddress mockAddress = mock(InetAddress.class);
            inetAddressMock.when(() -> InetAddress.getAllByName("safe.com"))
                .thenReturn(new InetAddress[]{mockAddress});
            org.mockito.Mockito.when(mockAddress.getHostAddress()).thenReturn("10.0.0.5");

            AgentStudioException exception = assertThrows(AgentStudioException.class,
                () -> checkUrlService.checkSwaggerUrlValid("http://safe.com/path", "10.0.0.0/24"));
            assertEquals(StudioError.WORKFLOW_ADDRESS_RESTRICTED, exception.getErrorCode());
        }
    }

    @Test
    void dnsResolvesToMultipleIps_oneInBlacklist_throwsWORKFLOW_ADDRESS_RESTRICTED() {
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
            InetAddress safeAddress = mock(InetAddress.class);
            InetAddress blacklistedAddress = mock(InetAddress.class);
            inetAddressMock.when(() -> InetAddress.getAllByName("safe.com"))
                .thenReturn(new InetAddress[]{safeAddress, blacklistedAddress});
            org.mockito.Mockito.when(safeAddress.getHostAddress()).thenReturn("203.0.113.1");
            org.mockito.Mockito.when(blacklistedAddress.getHostAddress()).thenReturn("10.0.0.1");

            AgentStudioException exception = assertThrows(AgentStudioException.class,
                () -> checkUrlService.checkSwaggerUrlValid("http://safe.com/path", "10.0.0.1"));
            assertEquals(StudioError.WORKFLOW_ADDRESS_RESTRICTED, exception.getErrorCode());
        }
    }

    @Test
    void blacklistWithSpacesAndCommas_parsedCorrectly() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> checkUrlService.checkSwaggerUrlValid("http://evil.com/path", " evil.com , 10.0.0.1 "));
        assertEquals(StudioError.WORKFLOW_ADDRESS_RESTRICTED, exception.getErrorCode());
    }
}
