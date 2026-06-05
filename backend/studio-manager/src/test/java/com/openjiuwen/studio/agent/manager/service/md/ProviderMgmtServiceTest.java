/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_WORKSPACE_ID;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.EN_LANGUAGE;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceProviderListRsp;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceProviderReq;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceProviderRsp;
import com.openjiuwen.studio.agent.manager.dto.QueryPlatformProvidersQo;
import com.openjiuwen.studio.agent.manager.dto.QueryUserProvidersQo;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.prompt.engineering.utils.HttpUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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

public class ProviderMgmtServiceTest extends BaseTest {
    @Autowired
    private ProviderMgmtService providerMgmtService;

    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<HttpUtil> mockedStaticHttp;

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
        mockedStaticHttp = Mockito.mockStatic(HttpUtil.class);
        mockedStaticHttp.when(HttpUtil::getLanguage).thenReturn(EN_LANGUAGE);
    }

    @BeforeEach
    public void beforeEach() {
        ReflectionTestUtils.setField(authService, "obsService", mgObsService);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedStaticHttp.close();
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void createModelServiceProvidersTest() {
        try {
            providerMgmtService.createModelServiceProviders(TEST_PROJECT_ID, "default",
                    new ModelServiceProviderReq().setProviderName("agentBuilder")
                            .setAuthType("API_KEY")
                            .setAuthInfo("{\"API Key\":\"test\"}"));
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.PROVIDER_NAME_REPEATED, e.getErrorCode());
        }

        ModelServiceProviderReq req = new ModelServiceProviderReq().setProviderName("testcase")
                .setProviderNameEn("testcase")
                .setAuthType("NO_AUTH")
                .setAuthInfo("{}");
        providerMgmtService.createModelServiceProviders(TEST_PROJECT_ID, "default", req);
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void deleteModelServiceProviderTest() {
        try {
            providerMgmtService.deleteModelServiceProvider(TEST_PROJECT_ID, "default_not_exist", "agentBuilder");
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_PROVIDER_NOT_EXIST, e.getErrorCode());
        }

        providerMgmtService.deleteModelServiceProvider(TEST_PROJECT_ID, "default", "agentBuilder");
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void integrationModelServiceProviderDetailTest() {
        ModelServiceProviderRsp rsp = providerMgmtService.integrationModelServiceProviderDetail(TEST_PROJECT_ID,
                "default", "agentBuilder");
        Assertions.assertNotNull(rsp);

        rsp = providerMgmtService.integrationModelServiceProviderDetail(TEST_PROJECT_ID,
                "default", "ab");
        Assertions.assertNotNull(rsp);
        Assertions.assertEquals("http://www.demo.com/v1/ab", rsp.getProviderUrl());
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void platformModelServiceProviderDetailTest() {
        ModelServiceProviderRsp rsp;
        try {
            rsp = providerMgmtService.platformModelServiceProviderDetail(TEST_PROJECT_ID, "agentBuilder", "default");
            Assertions.assertNotNull(rsp);
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_PROVIDER_NOT_EXIST, e.getErrorCode());
        }

        rsp = providerMgmtService.platformModelServiceProviderDetail(TEST_PROJECT_ID, "100", "default");
        Assertions.assertNotNull(rsp);
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void queryUserProvidersTest() {
        ModelServiceProviderListRsp rsp = providerMgmtService.queryUserProviders(TEST_PROJECT_ID,
                new QueryUserProvidersQo().setAuthConfigStatus("part_available"));
        Assertions.assertEquals(0, rsp.getTotal());

        rsp = providerMgmtService.queryUserProviders(TEST_PROJECT_ID, new QueryUserProvidersQo().setModelName("NN"));
        Assertions.assertEquals(0, rsp.getTotal());

        rsp = providerMgmtService.queryUserProviders(TEST_PROJECT_ID, new QueryUserProvidersQo());
        Assertions.assertEquals(0, rsp.getTotal());
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void queryPlatformProvidersTest() {
        ModelServiceProviderListRsp rsp = providerMgmtService.queryPlatformProviders(TEST_PROJECT_ID,
                new QueryPlatformProvidersQo().setModelName("mm"));
        Assertions.assertEquals(1, rsp.getTotal());

        rsp = providerMgmtService.queryPlatformProviders(TEST_PROJECT_ID,
                new QueryPlatformProvidersQo().setModelName("mm").setAuthConfigStatus("part_available"));
        Assertions.assertEquals(0, rsp.getTotal());

        rsp = providerMgmtService.queryPlatformProviders(TEST_PROJECT_ID, new QueryPlatformProvidersQo());
        Assertions.assertEquals(1, rsp.getTotal());

        rsp = providerMgmtService.queryPlatformProviders(TEST_PROJECT_ID,
                new QueryPlatformProvidersQo().setAuthConfigStatus("available"));
        Assertions.assertEquals(0, rsp.getTotal());

        rsp = providerMgmtService.queryPlatformProviders(TEST_PROJECT_ID,
                new QueryPlatformProvidersQo().setAuthConfigStatus("no_exist"));
        Assertions.assertEquals(1, rsp.getTotal());
    }
}
