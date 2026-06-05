/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBriefRsp;
import com.openjiuwen.studio.agent.common.dto.agent.node.DatasourceConnectionInfo;
import com.openjiuwen.studio.agent.manager.dto.DatasourceInfoReq;
import com.openjiuwen.studio.agent.manager.dto.DatasourceInfoRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListDatasourceQo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveDatasourceTableColumnsQo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveDatasourceTablesQo;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * DatasourceManagementService测试类
 *
 */
@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
@Slf4j
public class DatasourceManagementServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<CryptoUtils> mockedCryptoUtils;

    @Autowired
    DatasourceManagementService datasourceManagementService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService obsService;

    @BeforeAll
    public static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(Constants.TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
        mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(Constants.TEST_DOMAIN_ID);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_USER_NAME);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_USER_ID);

        mockedCryptoUtils = Mockito.mockStatic(CryptoUtils.class);
        mockedCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("decrypt");
        mockedCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("encrypt");
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedCryptoUtils.close();
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(datasourceManagementService, "obsService", obsService);
        ReflectionTestUtils.setField(datasourceManagementService, "enableUrlCheck", false);
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
    }

    @Test
    void testCreateDatasource() {
        DatasourceInfoReq datasourceInfoReq = initDatasourceInfoReq();
        DatasourceBriefRsp datasourceBriefRsp =
            datasourceManagementService.createDatasource(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, datasourceInfoReq);
        DatasourceInfoRsp datasourceInfoRsp =
            datasourceManagementService.retrieveDatasource(Constants.TEST_PROJECT_ID, datasourceBriefRsp.getId(),
                Constants.TEST_WORKSPACE_ID);
        Assertions.assertEquals("******", datasourceInfoRsp.getConnectionInfo().getPassword());
    }

    @Test
    void testModifyCustomModel() {
        DatasourceInfoReq datasourceInfoReq = initDatasourceInfoReq();
        DatasourceBriefRsp datasourceBriefRsp =
            datasourceManagementService.createDatasource(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, datasourceInfoReq);

        datasourceInfoReq.setName("xx");
        datasourceManagementService.modifyDatasource(Constants.TEST_PROJECT_ID, datasourceBriefRsp.getId(),
            Constants.TEST_WORKSPACE_ID, datasourceInfoReq);
        DatasourceListRsp datasourceListRsp = datasourceManagementService.listDatasource(Constants.TEST_PROJECT_ID,
            new ListDatasourceQo().setOffset(0).setLimit(10).setName("xx"));
        Assertions.assertEquals(1L, (long) datasourceListRsp.getCount());
        Assertions.assertEquals("xx", datasourceListRsp.getDatasourceList().get(0).getName());

        datasourceManagementService.deleteDatasource(Constants.TEST_PROJECT_ID, datasourceBriefRsp.getId(),
            Constants.TEST_WORKSPACE_ID);

        Assertions.assertEquals(0L,
            datasourceManagementService
                .listDatasource(Constants.TEST_PROJECT_ID, new ListDatasourceQo().setOffset(0).setLimit(10))
                .getCount());
    }

    @Test
    void testRetrieveDatasourceTables() throws AgentStudioException {
        DatasourceInfoReq datasourceInfoReq = initDatasourceInfoReq();
        DatasourceBriefRsp datasourceBriefRsp =
                datasourceManagementService.createDatasource(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, datasourceInfoReq);
        RetrieveDatasourceTablesQo retrieveDatasourceTablesQo = new RetrieveDatasourceTablesQo();
        retrieveDatasourceTablesQo.setWorkspaceId(Constants.TEST_WORKSPACE_ID);
        assertThrows(AgentStudioException.class, () -> datasourceManagementService.retrieveDatasourceTables(Constants.TEST_PROJECT_ID, datasourceBriefRsp.getId(), retrieveDatasourceTablesQo));
    }

    @Test
    void testCheckHost() throws AgentStudioException {
        ReflectionTestUtils.setField(datasourceManagementService, "enableUrlCheck", true);
        ReflectionTestUtils.setField(datasourceManagementService, "connectorConfigHostBlacklist", "localhost,127.0.0.0/8,fake");
        DatasourceInfoReq datasourceInfoReq = initDatasourceInfoReq();
        assertThrows(AgentStudioException.class, () -> datasourceManagementService.createDatasource(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, datasourceInfoReq));
        datasourceInfoReq.setConnectionInfo(datasourceInfoReq.getConnectionInfo().setHost("localhost"));
        assertThrows(AgentStudioException.class, () -> datasourceManagementService.createDatasource(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, datasourceInfoReq));
        ReflectionTestUtils.setField(datasourceManagementService, "connectorConfigHostBlacklist", "127.0.0.1");
        assertThrows(AgentStudioException.class, () -> datasourceManagementService.createDatasource(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, datasourceInfoReq));
    }

    @Test
    void retrieveDatasourceTableColumns() throws AgentStudioException {
        DatasourceInfoReq datasourceInfoReq = initDatasourceInfoReq();
        DatasourceBriefRsp datasourceBriefRsp =
                datasourceManagementService.createDatasource(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, datasourceInfoReq);
        RetrieveDatasourceTableColumnsQo retrieveDatasourceTablesQo = new RetrieveDatasourceTableColumnsQo();
        retrieveDatasourceTablesQo.setWorkspaceId(Constants.TEST_WORKSPACE_ID);
        assertThrows(AgentStudioException.class, () -> datasourceManagementService.retrieveDatasourceTableColumns(Constants.TEST_PROJECT_ID, datasourceBriefRsp.getId(), "test", retrieveDatasourceTablesQo));
    }

    @Test
    @Sql(scripts = {"classpath:sql/datasource_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testDBHealthCheck() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
        DatasourceInfoReq datasourceInfoReq = initDatasourceInfoReq();
        datasourceInfoReq.setName("xx");
        DatasourceBriefRsp datasourceBriefRsp = datasourceManagementService.modifyDatasource(Constants.TEST_PROJECT_ID,
            Constants.TEST_WORKFLOW_ID, Constants.TEST_WORKSPACE_ID, datasourceInfoReq);
        Assertions.assertEquals("failed", datasourceBriefRsp.getStatus());
    }

    private DatasourceInfoReq initDatasourceInfoReq() {
        DatasourceConnectionInfo datasourceConnectionInfo = new DatasourceConnectionInfo().setHost("fake_ip")
            .setDatabaseName("database")
            .setUser("fake_user")
            .setPassword("fake_password")
            .setSslEnabled(false)
            .setSqlVersion("1.1.1");
        return new DatasourceInfoReq().setName("数据源1")
            .setType("MYSQL")
            .setDesc("描述1")
            .setInternetAccess("public")
            .setConnectionInfo(datasourceConnectionInfo);
    }
}
