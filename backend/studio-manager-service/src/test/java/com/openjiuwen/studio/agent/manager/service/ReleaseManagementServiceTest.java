/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.ReleaseAppInfo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveReleaseAppInfoQo;
import com.openjiuwen.studio.agent.manager.entity.ReleaseChannel;
import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseChannelMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReleaseManagementServiceTest {

    @Mock
    private ReleaseVersionMapper releaseVersionMapper;

    @Mock
    private ReleaseChannelMapper releaseChannelMapper;

    @Mock
    private MgObsService mgObsService;

    @Mock
    private WorkflowMapper workflowMapper;

    private ReleaseManagementService releaseManagementService;

    @BeforeEach
    void setUp() {
        releaseManagementService = new ReleaseManagementService(
            releaseVersionMapper, releaseChannelMapper, mgObsService, workflowMapper);
    }

    @Test
    void testRetrieveReleaseAppInfo_ChannelNotFound() {
        RetrieveReleaseAppInfoQo qo = new RetrieveReleaseAppInfoQo();
        qo.setFindBy("channel_id");
        qo.setChannelType("web");

        when(releaseChannelMapper.selectByChannelIdOrShortCode(nullable(String.class), nullable(String.class),
            nullable(String.class), nullable(String.class)))
            .thenReturn(null);

        assertThrows(AgentStudioException.class,
            () -> releaseManagementService.retrieveReleaseAppInfo("proj-1", "ch-1", qo));
    }

    @Test
    void testRetrieveReleaseAppInfo_VersionNotFound() {
        RetrieveReleaseAppInfoQo qo = new RetrieveReleaseAppInfoQo();
        qo.setFindBy("channel_id");
        qo.setChannelType("web");

        ReleaseChannel channel = new ReleaseChannel();
        channel.setAppId("app-1");
        channel.setVersionId("v-1");
        when(releaseChannelMapper.selectByChannelIdOrShortCode(nullable(String.class), nullable(String.class),
            nullable(String.class), nullable(String.class)))
            .thenReturn(channel);
        when(releaseVersionMapper.selectByAppIdAndVersionId("app-1", "v-1")).thenReturn(null);

        assertThrows(AgentStudioException.class,
            () -> releaseManagementService.retrieveReleaseAppInfo("proj-1", "ch-1", qo));
    }

    @Test
    void testRetrieveReleaseAppInfo_AgentApp() {
        RetrieveReleaseAppInfoQo qo = new RetrieveReleaseAppInfoQo();
        qo.setFindBy("channel_id");
        qo.setChannelType("web");

        ReleaseChannel channel = new ReleaseChannel();
        channel.setAppId("app-1");
        channel.setVersionId("v-1");
        when(releaseChannelMapper.selectByChannelIdOrShortCode(nullable(String.class), nullable(String.class),
            nullable(String.class), nullable(String.class)))
            .thenReturn(channel);

        ReleaseVersion version = new ReleaseVersion();
        version.setVersionId("v-1");
        version.setAppId("app-1");
        version.setAppType(CommonConstant.AGENT);
        version.setDslPath("dsl/path");
        when(releaseVersionMapper.selectByAppIdAndVersionId("app-1", "v-1")).thenReturn(version);

        String agentDsl = "{\"name\":\"TestAgent\",\"description\":\"desc\",\"icon\":\"icon.png\",\"prologue\":\"hello\",\"suggest_queries\":[\"q1\"]}";
        when(mgObsService.downloadObsFile("dsl/path")).thenReturn(agentDsl);

        ReleaseAppInfo result = releaseManagementService.retrieveReleaseAppInfo("proj-1", "ch-1", qo);
        assertNotNull(result);
        assertEquals("TestAgent", result.getName());
        assertEquals("v-1", result.getVersionId());
    }

    @Test
    void testRetrieveReleaseAppInfo_ShortCodeFindBy() {
        RetrieveReleaseAppInfoQo qo = new RetrieveReleaseAppInfoQo();
        qo.setFindBy("short_code");
        qo.setChannelType("web");

        ReleaseChannel channel = new ReleaseChannel();
        channel.setAppId("app-1");
        channel.setVersionId("v-1");
        when(releaseChannelMapper.selectByChannelIdOrShortCode(nullable(String.class), nullable(String.class),
            nullable(String.class), nullable(String.class)))
            .thenReturn(channel);

        ReleaseVersion version = new ReleaseVersion();
        version.setVersionId("v-1");
        version.setAppId("app-1");
        version.setAppType(CommonConstant.AGENT);
        version.setDslPath("dsl/path");
        when(releaseVersionMapper.selectByAppIdAndVersionId("app-1", "v-1")).thenReturn(version);

        String agentDsl = "{\"name\":\"TestAgent\",\"description\":\"desc\"}";
        when(mgObsService.downloadObsFile("dsl/path")).thenReturn(agentDsl);

        ReleaseAppInfo result = releaseManagementService.retrieveReleaseAppInfo("proj-1", "sc-1", qo);
        assertNotNull(result);
    }
}
