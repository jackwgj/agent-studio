/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_WORKSPACE_ID;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.ProviderAuthConfig;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

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

import java.util.List;

public class ProviderAuthServiceTest extends BaseTest {

    @Autowired
    private ProviderAuthService providerAuthService;

    @Autowired
    private ProviderAuthDataMapper authMapper;

    private static MockedStatic<RequestContextUtils> mockedStatic;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService mgObsService;

    @BeforeAll
    public static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
        mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(Constants.TEST_DOMAIN_ID);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_USER_NAME);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_USER_ID);
        mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(TEST_WORKSPACE_ID);
    }

    @BeforeEach
    public void beforeEach() {
        ReflectionTestUtils.setField(providerAuthService, "obsService", mgObsService);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_auth_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void createAuthInfoTest() {
        ProviderAuthData authData = new ProviderAuthData();
        authData.setId("test_auth_id_001");
        authData.setProviderId("test_provider_001");
        authData.setAuthMetadataId("test_auth_meta_001");
        authData.setAuthType("API_KEY");
        authData.setAuthInfo("test_api_key_12345");
        authData.setCreatedByUser("test_user");
        authData.setCreatedDate(System.currentTimeMillis());
        authData.setLastUpdatedDate(System.currentTimeMillis());
        authData.setDomainId("test_domain_001");
        authData.setProjectId("test_project_001");
        authData.setWorkspaceId("test_workspace_001");
        authData.setIdentityId("test_identity_001");
        providerAuthService.createAuthInfo(authData);

        ProviderAuthData result = authMapper.selectById("test_auth_id_001");
        Assertions.assertNotNull(result);
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_auth_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updateAuthInfoTest() {

        ProviderAuthData authData = new ProviderAuthData();
        authData.setId("111");
        authData.setAuthType("API_KEY");
        authData.setAuthInfo("test_api_key_12345");
        authData.setLastUpdatedDate(System.currentTimeMillis());
        providerAuthService.updateAuthInfo(authData);

        ProviderAuthData result = authMapper.selectById("111");
        Assertions.assertEquals(result.getAuthInfo(), "test_api_key_12345");
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_auth_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void deleteByProviderTest() {
        providerAuthService.deleteByProvider("test_project_id", "default", "100");

        List<ProviderAuthData> list = authMapper.selectByProviderId("test_project_id", "default", "100");
        Assertions.assertTrue(list == null || list.isEmpty() || list.get(0).getAuthInfo() == null);
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_auth_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void deleteByIdTest() {
        providerAuthService.deleteById("default", "100", "111");
        ProviderAuthData result = authMapper.selectById("111");
        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getAuthInfo());
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_auth_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void transToAuthConfigTest() {
        ProviderAuthMetadata metadata = new ProviderAuthMetadata();
        metadata.setAuthType("API_KEY");
        metadata.setAuthInfo("{\"API Key\":\"\"}");
        metadata.setProviderId("100");
        metadata.setId("1023");
        ProviderAuthConfig rspApi = providerAuthService.transToAuthConfig(metadata);
        Assertions.assertNotNull(rspApi);
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_auth_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void selectProviderMetadataAndPermissionCheckTest() {
        // 测试场景1：元数据不存在
        try {
            providerAuthService.selectProviderMetadataAndPermissionCheck("100", "111", "99", true);
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_AUTH_METADATA_NOT_EXIST, e.getErrorCode());
        }

        // 测试场景2：项目权限不足
        try {
            providerAuthService.selectProviderMetadataAndPermissionCheck("100", "111", "100", false);
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_USER_NOT_PERMISSION_OPERATE, e.getErrorCode());
        }

        // 测试场景3：工作区权限不足
        try {
            providerAuthService.selectProviderMetadataAndPermissionCheck("100", "112", "102", true);
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_USER_NOT_PERMISSION_OPERATE, e.getErrorCode());
        }

        // 测试场景4：权限充足，成功返回元数据
        List<ProviderAuthMetadata> metadatas = providerAuthService.selectProviderMetadataAndPermissionCheck(
            "test_project_id", "test", "102", true);
        // 添加断言，验证返回的元数据是否符合预期
        Assertions.assertNotNull(metadatas);
        Assertions.assertFalse(metadatas.isEmpty());
    }

    @Test
    @Sql(scripts = {"classpath:sql/provider_auth_test_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void syncUpgradeAuthDatasTest() {
        try {
            ReflectionTestUtils.setField(providerAuthService, "upgradeSyncEnable", true);
            providerAuthService.postConstruct();

            providerAuthService.syncUpgradeAuthDatas();
        } finally {
            ReflectionTestUtils.setField(providerAuthService, "upgradeSyncEnable", false);
            providerAuthService.postConstruct();
        }
    }
}
