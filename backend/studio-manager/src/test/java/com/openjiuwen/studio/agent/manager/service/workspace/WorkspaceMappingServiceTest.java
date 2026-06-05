/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.manager.dto.Extension;
import com.openjiuwen.studio.agent.manager.dto.ExternalMappingInfo;
import com.openjiuwen.studio.agent.manager.dto.SecretLevel;
import com.openjiuwen.studio.agent.manager.entity.workspace.WorkspaceMappingEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMappingMapper;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class WorkspaceMappingServiceTest {

    @Mock
    private WorkspaceMappingMapper workspaceMappingMapper;

    @InjectMocks
    private WorkspaceMappingService workspaceMappingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
    }

    @Test
    void testQueryWorkspaceMappingInfoByWorkspaceId_empty() {
        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceId("ws1")).thenReturn(
            Collections.emptyList());

        List<ExternalMappingInfo> result = workspaceMappingService.queryWorkspaceMappingInfoByWorkspaceId("ws1");

        assertTrue(result.isEmpty());
    }

    @Test
    void testQueryWorkspaceMappingInfoByWorkspaceId() {
        WorkspaceMappingEntity entity = new WorkspaceMappingEntity();
        entity.setWorkspaceId("ws1");
        entity.setMappingId("m1");
        entity.setSource("git");

        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceId("ws1")).thenReturn(
            Collections.singletonList(entity));

        List<ExternalMappingInfo> result = workspaceMappingService.queryWorkspaceMappingInfoByWorkspaceId("ws1");

        assertEquals(1, result.size());
        assertEquals("m1", result.get(0).getMappingId());
    }

    @Test
    void testQueryMappingWorkspaceExtensionInfo_empty() {
        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceIdAndSource("ws1", "git")).thenReturn(null);

        List<Extension> res = workspaceMappingService.queryMappingWorkspaceExtensionInfo("ws1", "git");

        assertTrue(res.isEmpty());
    }

    @Test
    void testQueryMappingWorkspaceExtensionInfo() throws Exception {
        List<Extension> extensionList = new ArrayList<>();
        Extension e = new Extension();
        e.setKey("token");
        e.setValue("abc");
        e.setSecretLevel(SecretLevel.NONE);
        extensionList.add(e);

        WorkspaceMappingEntity entity = new WorkspaceMappingEntity();
        entity.setExtensionContent(objectMapper.writeValueAsString(extensionList));

        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceIdAndSource("ws1", "git")).thenReturn(
            entity);

        List<Extension> result = workspaceMappingService.queryMappingWorkspaceExtensionInfo("ws1", "git");

        assertEquals(1, result.size());
        assertEquals("token", result.get(0).getKey());
    }

    @Test
    void testAssociateWorkspaceRelationWithExternalSystem_update() {
        WorkspaceMappingEntity existed = new WorkspaceMappingEntity();
        existed.setId("id1");
        existed.setWorkspaceId("ws1");
        existed.setSource("git");

        ExternalMappingInfo info = new ExternalMappingInfo();
        info.setWorkspaceId("ws1");
        info.setSource("git");
        info.setMappingId("newMid");
        info.setExtensionContent(Collections.emptyList());

        when(workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceIdAndSource("ws1", "git")).thenReturn(
            existed);

        workspaceMappingService.associateWorkspaceRelationWithExternalSystem("ws1", info);
    }

    @Test
    void testEncryptedExtension2String_encrypt() throws Exception {
        Extension ext = new Extension();
        ext.setKey("pwd");
        ext.setValue("123");
        ext.setSecretLevel(SecretLevel.ENCRYPTION);

        try (var mock = Mockito.mockStatic(CryptoUtils.class)) {
            mock.when(() -> CryptoUtils.encrypt("123")).thenReturn("test");

            List<Extension> list = Collections.singletonList(ext);
            String json = workspaceMappingService.encryptedExtension2String(list);

            List<Extension> result = objectMapper.readValue(json, new TypeReference<List<Extension>>() {});

            assertEquals("test", result.get(0).getValue());
        }
    }
}
