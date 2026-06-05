/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_WORKSPACE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ProviderAuthMgmtServiceTest extends BaseTest {

    @Autowired
    private ProviderAuthMgmtService authMgmtService;

    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<CryptoUtils> mockedCryptoUtils;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService mgObsService;

    @Autowired
    private ProviderAuthService authService;

    @BeforeAll
    public static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
        mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(Constants.TEST_DOMAIN_ID);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_USER_NAME);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_USER_ID);
        mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(TEST_WORKSPACE_ID);

        mockedCryptoUtils = Mockito.mockStatic(CryptoUtils.class);
        mockedCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("decrypt");
        mockedCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("encrypt");
    }

    @BeforeEach
    public void beforeEach() {
        ReflectionTestUtils.setField(authService, "obsService", mgObsService);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedCryptoUtils.close();
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_auth_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void removeAuthConfigTest() {
        try {
            authMgmtService.removeAuthConfig(TEST_PROJECT_ID, "default", "not_exsit");
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_AUTH_DATA_NOT_EXIST, e.getErrorCode());
        }
        try {
            authMgmtService.removeAuthConfig(TEST_PROJECT_ID, "default", "123");
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_USER_NO_PERMISSION_DELETE, e.getErrorCode());
        }
        try {
            authMgmtService.removeAuthConfig(TEST_PROJECT_ID, "default", "321");
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_USER_NO_PERMISSION_DELETE, e.getErrorCode());
        }

        authMgmtService.removeAuthConfig(TEST_PROJECT_ID, "default", "111");
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_auth_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void removeProviderAuthCfgTest() {
        try {
            authMgmtService.removeProviderAuthCfg(TEST_PROJECT_ID, "default", "", "");
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_REQUEST_PARAM_AUTH_INVALID, e.getErrorCode());
        }

        authMgmtService.removeProviderAuthCfg(TEST_PROJECT_ID, "default", "", "111");

        authMgmtService.removeProviderAuthCfg(TEST_PROJECT_ID, "default", "100", "");
    }
}
