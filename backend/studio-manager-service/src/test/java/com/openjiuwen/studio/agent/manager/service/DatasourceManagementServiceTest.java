/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteReq;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceInfoReq;
import com.openjiuwen.studio.agent.manager.dto.DatasourceListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListDatasourceQo;
import com.openjiuwen.studio.agent.common.dto.agent.node.DatasourceConnectionInfo;
import com.openjiuwen.studio.agent.manager.entity.DatasourceEntity;
import com.openjiuwen.studio.agent.manager.mapper.DatasourceMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DatasourceManagementServiceTest {

    private DatasourceMapper datasourceMapper;
    private MgObsService obsService;
    private DatasourceStatusCheckService datasourceStatusCheckService;

    private DatasourceManagementService datasourceManagementService;

    @BeforeEach
    void setUp() {
        datasourceMapper = mock(DatasourceMapper.class);
        obsService = mock(MgObsService.class);
        datasourceStatusCheckService = mock(DatasourceStatusCheckService.class);

        MockitoAnnotations.openMocks(this);
        datasourceManagementService = new DatasourceManagementService();
        ReflectionTestUtils.setField(datasourceManagementService, "datasourceMapper", datasourceMapper);
        ReflectionTestUtils.setField(datasourceManagementService, "obsService", obsService);
        ReflectionTestUtils.setField(datasourceManagementService, "datasourceStatusCheckService", datasourceStatusCheckService);
        ReflectionTestUtils.setField(datasourceManagementService, "datasourceIrPath", "/ir/path");
        ReflectionTestUtils.setField(datasourceManagementService, "columnTemplate", "");
        ReflectionTestUtils.setField(datasourceManagementService, "enableUrlCheck", false);
        ReflectionTestUtils.setField(datasourceManagementService, "connectorConfigHostBlacklist", "");
    }

    @Test
    void testListDatasource_Success() {
        ListDatasourceQo qo = new ListDatasourceQo();
        qo.setWorkspaceId("w1");
        qo.setOffset(0);
        qo.setLimit(10);

        when(datasourceMapper.getDatasourceEntity(any(), any())).thenReturn(Collections.emptyList());

        DatasourceListRsp result = datasourceManagementService.listDatasource("p1", qo);

        assertNotNull(result);
    }

    @Test
    void testBatchDeleteDatasource_Success() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            DatasourceEntity entity = DatasourceEntity.builder()
                .id("ds-1")
                .projectId("p1")
                .workspaceId("w1")
                .build();
            when(datasourceMapper.getByIds(eq(List.of("ds-1")))).thenReturn(List.of(entity));

            DatasourceBatchDeleteReq req = new DatasourceBatchDeleteReq();
            req.setIds(List.of("ds-1"));

            DatasourceBatchDeleteRsp result = datasourceManagementService.batchDeleteDatasource("p1", "w1", req);

            assertNotNull(result);
            verify(datasourceMapper).deleteByIds(anyList(), eq("p1"));
        }
    }

    @Test
    void testBatchDeleteDatasource_EmptyIds() {
        DatasourceBatchDeleteReq req = new DatasourceBatchDeleteReq();
        req.setIds(Collections.emptyList());

        assertThrows(AgentStudioException.class, () ->
            datasourceManagementService.batchDeleteDatasource("p1", "w1", req));
    }

    @Test
    void testDeleteDatasource_Success() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            DatasourceEntity entity = DatasourceEntity.builder()
                .id("ds-1")
                .projectId("p1")
                .workspaceId("w1")
                .build();
            when(datasourceMapper.getByIds(eq(List.of("ds-1")))).thenReturn(List.of(entity));

            var result = datasourceManagementService.deleteDatasource("p1", "ds-1", "w1");

            assertNotNull(result);
        }
    }

    @Test
    void testRetrieveDatasource_Success() {
        DatasourceEntity entity = DatasourceEntity.builder()
            .id("ds-1")
            .projectId("p1")
            .workspaceId("w1")
            .name("Test DS")
            .type("mysql")
            .build();
        when(datasourceMapper.getByPrimaryKeyAndWorkspaceId("ds-1", "p1", "w1")).thenReturn(entity);
        when(obsService.downloadObsFile(anyString())).thenReturn("{}");

        var result = datasourceManagementService.retrieveDatasource("p1", "ds-1", "w1");

        assertNotNull(result);
    }

    @Test
    void testRetrieveDatasource_NotFound() {
        when(datasourceMapper.getByPrimaryKeyAndWorkspaceId("ds-1", "p1", "w1")).thenReturn(null);

        var result = datasourceManagementService.retrieveDatasource("p1", "ds-1", "w1");

        assertNotNull(result);
    }
}
