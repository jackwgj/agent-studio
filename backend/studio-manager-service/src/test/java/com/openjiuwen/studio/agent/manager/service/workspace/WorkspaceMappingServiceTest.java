/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.workspace;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.Extension;
import com.openjiuwen.studio.agent.manager.dto.ExternalMappingInfo;
import com.openjiuwen.studio.agent.manager.dto.SecretLevel;
import com.openjiuwen.studio.agent.manager.entity.workspace.WorkspaceMappingEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMappingMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceMappingServiceTest {

    @Mock
    private WorkspaceMappingMapper workspaceMappingMapper;

    @InjectMocks
    private WorkspaceMappingService workspaceMappingService;

    @Test
    void testQueryWorkspaceMappingInfoByWorkspaceId_EmptyResult() {
        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceId("ws1")).thenReturn(new ArrayList<>());

        List<ExternalMappingInfo> result = workspaceMappingService.queryWorkspaceMappingInfoByWorkspaceId("ws1");
        assertTrue(result.isEmpty());
    }

    @Test
    void testQueryWorkspaceMappingInfoByWorkspaceId_WithData() {
        WorkspaceMappingEntity entity = new WorkspaceMappingEntity();
        entity.setWorkspaceId("ws1");
        entity.setSource("external");
        entity.setMappingId("m1");
        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceId("ws1")).thenReturn(List.of(entity));

        List<ExternalMappingInfo> result = workspaceMappingService.queryWorkspaceMappingInfoByWorkspaceId("ws1");
        assertEquals(1, result.size());
    }

    @Test
    void testQueryMappingWorkspaceExtensionInfo_NoMapping() {
        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceIdAndSource("ws1", "src1"))
            .thenReturn(null);

        List<Extension> result = workspaceMappingService.queryMappingWorkspaceExtensionInfo("ws1", "src1");
        assertTrue(result.isEmpty());
    }

    @Test
    void testQueryMappingWorkspaceExtensionInfo_ValidJson() {
        WorkspaceMappingEntity entity = new WorkspaceMappingEntity();
        entity.setExtensionContent("[{\"key\":\"k1\",\"value\":\"v1\",\"secretLevel\":\"PLAIN\"}]");
        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceIdAndSource("ws1", "src1"))
            .thenReturn(entity);

        List<Extension> result = workspaceMappingService.queryMappingWorkspaceExtensionInfo("ws1", "src1");
        assertEquals(1, result.size());
        assertEquals("k1", result.get(0).getKey());
    }

    @Test
    void testQueryMappingWorkspaceExtensionInfo_InvalidJson() {
        WorkspaceMappingEntity entity = new WorkspaceMappingEntity();
        entity.setExtensionContent("invalid json");
        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceIdAndSource("ws1", "src1"))
            .thenReturn(entity);

        assertThrows(AgentStudioException.class, () ->
            workspaceMappingService.queryMappingWorkspaceExtensionInfo("ws1", "src1"));
    }

    @Test
    void testAssociateWorkspaceRelationWithExternalSystem_NullInfo() {
        workspaceMappingService.associateWorkspaceRelationWithExternalSystem("ws1", null);
        verify(workspaceMappingMapper, never()).insert(any());
    }

    @Test
    void testAssociateWorkspaceRelationWithExternalSystem_MismatchedWorkspaceId() {
        ExternalMappingInfo info = new ExternalMappingInfo();
        info.setWorkspaceId("ws2");

        assertThrows(AgentStudioException.class, () ->
            workspaceMappingService.associateWorkspaceRelationWithExternalSystem("ws1", info));
    }

    @Test
    void testAssociateWorkspaceRelationWithExternalSystem_NewMapping() {
        ExternalMappingInfo info = new ExternalMappingInfo();
        info.setWorkspaceId("ws1");
        info.setSource("src1");
        info.setMappingId("m1");
        info.setExtensionContent(new ArrayList<>());

        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceIdAndSource("ws1", "src1"))
            .thenReturn(null);

        workspaceMappingService.associateWorkspaceRelationWithExternalSystem("ws1", info);
        verify(workspaceMappingMapper).insert(any(WorkspaceMappingEntity.class));
    }

    @Test
    void testAssociateWorkspaceRelationWithExternalSystem_ExistingMapping() {
        ExternalMappingInfo info = new ExternalMappingInfo();
        info.setWorkspaceId("ws1");
        info.setSource("src1");
        info.setMappingId("m1");
        info.setExtensionContent(new ArrayList<>());

        WorkspaceMappingEntity existing = new WorkspaceMappingEntity();
        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceIdAndSource("ws1", "src1"))
            .thenReturn(existing);

        workspaceMappingService.associateWorkspaceRelationWithExternalSystem("ws1", info);
        verify(workspaceMappingMapper).updateByPrimaryKey(any(WorkspaceMappingEntity.class));
    }

    @Test
    void testEncryptedExtension2String_PlainText() {
        Extension ext = new Extension();
        ext.setKey("k1");
        ext.setValue("v1");
        ext.setSecretLevel(SecretLevel.NONE);

        String result = workspaceMappingService.encryptedExtension2String(List.of(ext));
        assertNotNull(result);
        assertTrue(result.contains("k1"));
        assertTrue(result.contains("v1"));
    }

    @Test
    void testEncryptedExtension2String_EmptyList() {
        String result = workspaceMappingService.encryptedExtension2String(new ArrayList<>());
        assertEquals("[]", result);
    }
}
