/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.manager.dto.AgentInfo;
import com.openjiuwen.studio.agent.manager.dto.ToolReference;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.enums.ResourceTypeEnum;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.plugin.IPluginBase;
import com.openjiuwen.studio.agent.manager.utils.TestUtil;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ExportResourceUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class AgentPluginAdapterTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IPluginBase pluginBaseImpl;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService obsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentMapper agentMapper;

    @InjectMocks
    private AgentPluginAdapter agentPluginAdapter;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void getToolVersion_WhenToolVersionIsNotEmpty_ShouldReturnToolVersion() throws Exception {
        // Given
        String pluginId = "test-plugin";
        String toolVersion = "1.0.0";

        // When
        String result = agentPluginAdapter.getToolVersion(pluginId, toolVersion);

        // Then
        assertEquals("1.0.0", result);
    }

    @ParameterizedTest
    @CsvSource( {
        "null, null, null", "test-plugin, null, null", "null, 1.0.0, 1.0.0", "test-plugin, 1.0.0, 1.0.0"
    })
    void getToolVersion_WhenParametersAreNullOrNotNulll_ShouldReturnExpectedValue(String pluginId, String toolVersion,
        String expected) throws Exception {
        // When
        String result = agentPluginAdapter.getToolVersion(pluginId, toolVersion);

        // Then
        if (expected == null) {
            assertEquals(toolVersion, result);
        } else {
            assertEquals(expected, result);
        }
    }

    @Test
    void getToolVersion_WhenToolVersionIsEmptyAndPluginHasVersion_ShouldReturnPluginVersion() throws Exception {
        // Given
        String pluginId = "test-plugin";
        String pluginVersion = "2.0.0";
        when(pluginBaseImpl.getLastVersion(pluginId)).thenReturn(pluginVersion);

        // When
        String result = agentPluginAdapter.getToolVersion(pluginId, null);

        // Then
        assertEquals(pluginVersion, result);
    }

    @Test
    void getToolVersion_WhenToolVersionIsEmptyAndPluginHasNoVersion_ShouldReturnNull() throws Exception {
        // Given
        String pluginId = "test-plugin";
        when(pluginBaseImpl.getLastVersion(pluginId)).thenReturn(null);

        // When
        String result = agentPluginAdapter.getToolVersion(pluginId, null);

        // Then
        assertEquals(null, result);
    }

    @Test
    void getToolVersionFromAgent_WhenResourceVersionIsNotEmpty_ShouldReturnResourceVersion() throws Exception {
        // Given
        ExportResourceUnit exportResourceUnit = new ExportResourceUnit();
        exportResourceUnit.setResourceVersion("1.0.0");
        exportResourceUnit.setAppType("SOME_TYPE");

        // When
        String result = agentPluginAdapter.getToolVersionFromAgent(exportResourceUnit);

        // Then
        assertEquals("1.0.0", result);
    }

    @Test
    void getToolVersionFromAgent_WhenAppTypeIsNotAgent_ShouldReturnResourceVersion() throws Exception {
        // Given
        ExportResourceUnit exportResourceUnit = new ExportResourceUnit();
        exportResourceUnit.setResourceId("test-tool");
        exportResourceUnit.setResourceVersion(null);
        exportResourceUnit.setAppId("test-app-id");
        exportResourceUnit.setAppType("SOME_TYPE");

        // When
        String result = agentPluginAdapter.getToolVersionFromAgent(exportResourceUnit);

        // Then
        assertEquals(null, result);
    }

    @Test
    void getToolVersionFromAgent_WhenAppTypeIsAgentAndAgentExistsAndHasMatchingTool_ShouldReturnToolVersion()
        throws Exception {
        // Given
        Agent agent = new Agent();
        agent.setDslPath("obs://test/dsl.json");
        when(agentMapper.selectById("test-app-id")).thenReturn(agent);

        AgentInfo agentInfo = new AgentInfo();
        ToolReference toolReference = new ToolReference();
        toolReference.setToolId("test-tool");
        toolReference.setLastVersionId("3.0.0");
        agentInfo.setTools(java.util.Arrays.asList(toolReference));
        when(obsService.downloadObsFile("obs://test/dsl.json")).thenReturn(JSONObject.toJSONString(agentInfo));

        ExportResourceUnit exportResourceUnit = new ExportResourceUnit();
        exportResourceUnit.setResourceId("test-tool");
        exportResourceUnit.setResourceVersion(null);
        exportResourceUnit.setAppId("test-app-id");
        exportResourceUnit.setAppType(ResourceTypeEnum.AGENT.toString());

        // When
        String result = agentPluginAdapter.getToolVersionFromAgent(exportResourceUnit);

        // Then
        assertEquals("3.0.0", result);
    }

    @Test
    void getToolVersionFromAgent_WhenAppTypeIsAgentAndAgentExistsButHasNoMatchingTool_ShouldReturnResourceVersion()
        throws Exception {
        // Given
        Agent agent = new Agent();
        agent.setDslPath("obs://test/dsl.json");
        when(agentMapper.selectById("test-app-id")).thenReturn(agent);

        AgentInfo agentInfo = new AgentInfo();
        ToolReference toolReference = new ToolReference();
        toolReference.setToolId("different-tool");
        toolReference.setLastVersionId("3.0.0");
        agentInfo.setTools(java.util.Arrays.asList(toolReference));
        when(obsService.downloadObsFile("obs://test/dsl.json")).thenReturn(JSONObject.toJSONString(agentInfo));

        ExportResourceUnit exportResourceUnit = new ExportResourceUnit();
        exportResourceUnit.setResourceId("test-tool");
        exportResourceUnit.setResourceVersion(null);
        exportResourceUnit.setAppId("test-app-id");
        exportResourceUnit.setAppType(ResourceTypeEnum.AGENT.toString());

        // When
        String result = agentPluginAdapter.getToolVersionFromAgent(exportResourceUnit);

        // Then
        assertEquals(null, result);
    }

    @Test
    void getToolVersionFromAgent_WhenAppTypeIsAgentAndAgentDoesNotExist_ShouldReturnResourceVersion() throws Exception {
        // Given
        String jsonAgent = TestUtil.getStringFromFile("classpath:response/export_agent_dsl.json");
        Agent agent = new Agent();
        agent.setDslPath("agent/dsl/b51efca2-1cd4-47c6-9f72-58732ec08235/b51efca2-1cd4-47c6-9f72-58732ec08235.json");
        when(agentMapper.selectById("test-app-id")).thenReturn(agent);
        when(obsService.downloadObsFile(
            eq("agent/dsl/b51efca2-1cd4-47c6-9f72-58732ec08235/b51efca2-1cd4-47c6-9f72-58732ec08235.json"))).thenReturn(
            jsonAgent);
        ExportResourceUnit exportResourceUnit = new ExportResourceUnit();
        exportResourceUnit.setResourceId("test-tool");
        exportResourceUnit.setResourceVersion(null);
        exportResourceUnit.setAppId("test-app-id");
        exportResourceUnit.setAppType(ResourceTypeEnum.AGENT.toString());

        // When
        String result = agentPluginAdapter.getToolVersionFromAgent(exportResourceUnit);

        // Then
        assertEquals(null, result);
    }
}
