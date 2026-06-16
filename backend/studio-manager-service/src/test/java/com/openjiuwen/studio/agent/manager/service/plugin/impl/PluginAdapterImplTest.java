/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.plugin.impl;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.ToolReference;
import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;
import com.openjiuwen.studio.agent.manager.entity.ShareResourceEntity;
import com.openjiuwen.studio.agent.manager.entity.ToolEntity;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginEntity;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.plugin.IPluginBase;
import com.openjiuwen.studio.agent.manager.service.share.ShareInnerService;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

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

class PluginAdapterImplTest {

    private ShareInnerService shareInnerService;
    private PluginMapper pluginMapper;
    private IPluginBase pluginBase;
    private MgObsService obsService;
    private ReleaseVersionMapper releaseVersionMapper;
    private EncryptionAdapter encryptionAdapter;
    private RedisClient redisClient;

    private PluginAdapterImpl pluginAdapter;

    @BeforeEach
    void setUp() {
        shareInnerService = mock(ShareInnerService.class);
        pluginMapper = mock(PluginMapper.class);
        pluginBase = mock(IPluginBase.class);
        obsService = mock(MgObsService.class);
        releaseVersionMapper = mock(ReleaseVersionMapper.class);
        encryptionAdapter = mock(EncryptionAdapter.class);
        redisClient = mock(RedisClient.class);

        MockitoAnnotations.openMocks(this);
        pluginAdapter = new PluginAdapterImpl();
        ReflectionTestUtils.setField(pluginAdapter, "shareInnerService", shareInnerService);
        ReflectionTestUtils.setField(pluginAdapter, "pluginMapper", pluginMapper);
        ReflectionTestUtils.setField(pluginAdapter, "pluginBase", pluginBase);
        ReflectionTestUtils.setField(pluginAdapter, "obsService", obsService);
        ReflectionTestUtils.setField(pluginAdapter, "releaseVersionMapper", releaseVersionMapper);
        ReflectionTestUtils.setField(pluginAdapter, "encryptionAdapter", encryptionAdapter);
        ReflectionTestUtils.setField(pluginAdapter, "redisClient", redisClient);
        ReflectionTestUtils.setField(pluginAdapter, "pluginMaxFreeTrialTimes", 10);
    }

    @Test
    void testIsNewTool_True() {
        PluginEntity plugin = new PluginEntity();
        plugin.setRequestInfo("{\"basic_info\": {}, \"tool_info\": []}");

        assertTrue(pluginAdapter.isNewTool(plugin));
    }

    @Test
    void testIsNewTool_False() {
        PluginEntity plugin = new PluginEntity();
        plugin.setRequestInfo("{\"tool_info\": []}");

        assertFalse(pluginAdapter.isNewTool(plugin));
    }

    @Test
    void testIsNewTool_Null() {
        assertFalse(pluginAdapter.isNewTool(null));
    }

    @Test
    void testSetupFreeTrialQuota_InnerFreePlugin() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            PluginEntity plugin = new PluginEntity();
            plugin.setPluginId("plugin-1");
            plugin.setType("inner");
            plugin.setIsFree(1);

            ToolReference toolRef = new ToolReference();
            when(redisClient.get(anyString())).thenReturn("5");

            pluginAdapter.setupFreeTrialQuota(plugin, toolRef, 10);

            assertEquals(10, toolRef.getLimit());
            assertEquals(5, toolRef.getUsage());
        }
    }

    @Test
    void testSetupFreeTrialQuota_InnerFreePlugin_NoUsage() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            PluginEntity plugin = new PluginEntity();
            plugin.setPluginId("plugin-1");
            plugin.setType("inner");
            plugin.setIsFree(1);

            ToolReference toolRef = new ToolReference();
            when(redisClient.get(anyString())).thenReturn(null);

            pluginAdapter.setupFreeTrialQuota(plugin, toolRef, 10);

            assertEquals(10, toolRef.getLimit());
            assertEquals(0, toolRef.getUsage());
        }
    }

    @Test
    void testSetupFreeTrialQuota_NonInnerPlugin() {
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginId("plugin-1");
        plugin.setType("custom");
        plugin.setIsFree(0);

        ToolReference toolRef = new ToolReference();

        pluginAdapter.setupFreeTrialQuota(plugin, toolRef, 10);

        assertNull(toolRef.getLimit());
    }

    @Test
    void testSetupFreeTrialQuota_NullIsFree() {
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginId("plugin-1");
        plugin.setType("inner");
        plugin.setIsFree(null);

        ToolReference toolRef = new ToolReference();

        pluginAdapter.setupFreeTrialQuota(plugin, toolRef, 10);

        assertNull(toolRef.getLimit());
    }

    @Test
    void testBuildResourceId_Success() {
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginId("plugin-1");
        plugin.setRequestInfo("{\"tool_info\": [{\"tool_id\": \"t1\"}, {\"tool_id\": \"t2\"}]}");

        List<String> result = pluginAdapter.buildResourceId(plugin);

        assertEquals(2, result.size());
        assertTrue(result.contains("plugin-1#t1"));
        assertTrue(result.contains("plugin-1#t2"));
    }

    @Test
    void testGetToolReferences_EmptyPluginIds() {
        when(shareInnerService.getOriginSharedInfoByResourceId(anyList())).thenReturn(Collections.emptyList());
        when(pluginMapper.selectByPrimaryIdAndToolIdsWithSearchCriteria(anyString(), anyString(), anyList(), any()))
            .thenReturn(Collections.emptyList());

        List<ToolReference> result = pluginAdapter.getToolReferences("p1", "w1", Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAnonymizeAuthInfo_NullAuthInfo() {
        var result = pluginAdapter.anonymizeAuthInfo(null);
        assertNull(result);
    }
}
