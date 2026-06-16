/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.dto.WorkflowVO;
import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.mapper.HistoryWorkflowMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class WorkflowCommonServiceTest {

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private HistoryWorkflowMapper historyWorkflowMapper;

    @Mock
    private IrAdapterService irAdapterService;

    @Mock
    private MgObsService mgObsService;

    @InjectMocks
    private WorkflowCommonService workflowCommonService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workflowCommonService, "opSvcProjectId", "op-project-id");
    }

    @Test
    void testGetWorkflowByWorkspaceAndOpProject_Success() {
        WorkflowEntity expected = new WorkflowEntity();
        expected.setId("wf-1");
        when(workflowMapper.getWorkflowEntity("proj-1", "op-project-id", "ws-1", "wf-1")).thenReturn(expected);

        WorkflowEntity result = workflowCommonService.getWorkflowByWorkspaceAndOpProject("proj-1", "ws-1", "wf-1");
        assertNotNull(result);
        assertEquals("wf-1", result.getId());
    }

    @Test
    void testGetWorkflowByWorkspaceAndOpProject_NotFound() {
        when(workflowMapper.getWorkflowEntity(anyString(), anyString(), anyString(), anyString())).thenReturn(null);

        WorkflowEntity result = workflowCommonService.getWorkflowByWorkspaceAndOpProject("proj-1", "ws-1", "wf-999");
        assertEquals(null, result);
    }

    @Test
    void testSoftDelete_Success() {
        WorkflowEntity entity = new WorkflowEntity();
        entity.setProjectId("proj-1");
        entity.setId("wf-1");

        workflowCommonService.softDelete(entity);

        verify(historyWorkflowMapper).insert(entity);
        verify(workflowMapper).deleteByPrimaryKey("proj-1", "wf-1");
    }

    @Test
    void testWorkflowDslToIr_WithoutVersionId() {
        WorkflowEntity entity = buildWorkflowEntity();
        WorkflowVO workflowVo = new WorkflowVO();

        Map<String, Object> irInfo = Map.of("key", "value");
        when(irAdapterService.adaptWorkflow(eq(workflowVo), any())).thenReturn(irInfo);
        when(mgObsService.uploadObsFile(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("obs-path");

        String result = workflowCommonService.workflowDslToIr("wf-1", entity, workflowVo, null);
        assertNotNull(result);
        assertEquals("obs-path", result);
    }

    @Test
    void testWorkflowDslToIr_WithVersionId() {
        WorkflowEntity entity = buildWorkflowEntity();
        WorkflowVO workflowVo = new WorkflowVO();

        Map<String, Object> irInfo = Map.of("key", "value");
        when(irAdapterService.adaptWorkflow(eq(workflowVo), any())).thenReturn(irInfo);
        when(mgObsService.uploadObsFile(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("obs-path-versioned");

        String result = workflowCommonService.workflowDslToIr("wf-1", entity, workflowVo, "v1");
        assertNotNull(result);
        assertEquals("obs-path-versioned", result);
    }

    @Test
    void testParseMetadata_Success() {
        WorkflowEntity entity = buildWorkflowEntity();

        Map<String, Object> metadata = workflowCommonService.parseMetadata(entity);

        assertNotNull(metadata);
        assertEquals("user-1", metadata.get("userId"));
        assertEquals("proj-1", metadata.get("projectId"));
        assertEquals("ws-1", metadata.get("workspaceId"));
        assertEquals("domain-1", metadata.get("domainId"));
        assertEquals("chat", metadata.get("type"));
    }

    @Test
    void testParseMetadata_NullFields() {
        WorkflowEntity entity = new WorkflowEntity();

        Map<String, Object> metadata = workflowCommonService.parseMetadata(entity);

        assertNotNull(metadata);
        assertTrue(metadata.containsKey("userId"));
        assertTrue(metadata.containsKey("projectId"));
    }

    private WorkflowEntity buildWorkflowEntity() {
        WorkflowEntity entity = new WorkflowEntity();
        entity.setCreatorId("user-1");
        entity.setProjectId("proj-1");
        entity.setWorkspaceId("ws-1");
        entity.setDomainId("domain-1");
        entity.setUpdatedAt(System.currentTimeMillis());
        entity.setPublishedAt(System.currentTimeMillis());
        entity.setVisibility("public");
        entity.setStatus("published");
        entity.setWorkflowType("chat");
        return entity;
    }
}
