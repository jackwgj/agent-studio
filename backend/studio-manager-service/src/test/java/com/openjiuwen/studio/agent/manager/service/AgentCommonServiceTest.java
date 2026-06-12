/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.entity.HistoryReleaseVersionEntity;
import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.HistoryAgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.HistoryReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

/**
 * AgentCommonServiceTest - 使用 MyBatis Mapper 进行单元测试
 */
public class AgentCommonServiceTest {

    @Mock
    private AgentMapper agentMapper;

    @Mock
    private MappingMapper mappingMapper;

    @Mock
    private ReleaseVersionMapper releaseVersionMapper;

    @Mock
    private HistoryAgentMapper historyAgentMapper;

    @Mock
    private HistoryReleaseVersionMapper historyReleaseVersionMapper;

    @InjectMocks
    private AgentCommonService agentCommonService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 设置必要的配置字段
        ReflectionTestUtils.setField(agentCommonService, "agentDefaultIcon", "default-icon.png");
        ReflectionTestUtils.setField(agentCommonService, "controllerDefaultIcon", "controller-icon.png");
        ReflectionTestUtils.setField(agentCommonService, "iconMaxSize", "100");
        ReflectionTestUtils.setField(agentCommonService, "maxCustomizeNodeSize", 100);
        ReflectionTestUtils.setField(agentCommonService, "opSvcProjectId", "test-project-id");
        ReflectionTestUtils.setField(agentCommonService, "envType", "test");
        ReflectionTestUtils.setField(agentCommonService, "modelObsPrefix", "/test/prefix");
    }

    @Test
    public void test_softDeleteReleaseVersionByAppId_not_exist() throws Exception {
        ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
        when(releaseVersion.getVersionId()).thenReturn("v1.0");
        when(releaseVersion.getAppId()).thenReturn("testAppId");

        when(releaseVersionMapper.selectByAppId("testAppId")).thenReturn(List.of(releaseVersion));
        when(releaseVersionMapper.deleteByAppId("testAppId")).thenReturn(0);
        // MyBatis 返回 null 而不是 Optional.empty()
        when(historyReleaseVersionMapper.findByAppIdAndVersionId("testAppId", "v1.0")).thenReturn(null);

        agentCommonService.softDeleteReleaseVersionByAppId("testAppId");

        verify(releaseVersionMapper, times(1)).selectByAppId("testAppId");
        verify(historyReleaseVersionMapper, times(1)).findByAppIdAndVersionId("testAppId", "v1.0");
        verify(historyReleaseVersionMapper, times(1)).insertBatch(anyList());
        verify(releaseVersionMapper, times(1)).deleteByAppId("testAppId");
    }

    @Test
    public void test_softDeleteReleaseVersionByAppId_exist() throws Exception {
        ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
        when(releaseVersion.getVersionId()).thenReturn("v1.0");
        when(releaseVersion.getAppId()).thenReturn("testAppId");

        HistoryReleaseVersionEntity existingHistory = mock(HistoryReleaseVersionEntity.class);

        when(releaseVersionMapper.selectByAppId(anyString())).thenReturn(List.of(releaseVersion));
        when(releaseVersionMapper.deleteByAppId(anyString())).thenReturn(0);
        // 当记录已存在时，返回实体对象（不插入新记录）
        when(historyReleaseVersionMapper.findByAppIdAndVersionId("testAppId", "v1.0")).thenReturn(existingHistory);

        agentCommonService.softDeleteReleaseVersionByAppId("testAppId");

        verify(releaseVersionMapper, times(1)).selectByAppId("testAppId");
        verify(historyReleaseVersionMapper, times(1)).findByAppIdAndVersionId("testAppId", "v1.0");
        // 已存在时不插入
        verify(historyReleaseVersionMapper, times(0)).insertBatch(anyList());
        verify(releaseVersionMapper, times(1)).deleteByAppId("testAppId");
    }

    @Test
    public void test_softDeleteReleaseVersionById() throws Exception {
        ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
        when(releaseVersion.getId()).thenReturn("id-123");
        when(releaseVersion.getVersionId()).thenReturn("v1.0");
        when(releaseVersion.getVersionName()).thenReturn("Version v1.0");
        when(releaseVersion.getAppId()).thenReturn("testAppId");
        when(releaseVersion.getAppType()).thenReturn("agent");
        when(releaseVersion.getStatus()).thenReturn("published");

        when(historyReleaseVersionMapper.insert(any(HistoryReleaseVersionEntity.class))).thenReturn(1);
        when(releaseVersionMapper.deleteByPrimaryKey(anyString())).thenReturn(1);

        agentCommonService.softDeleteReleaseVersionById(releaseVersion);

        verify(historyReleaseVersionMapper, times(1)).insert(any(HistoryReleaseVersionEntity.class));
        verify(releaseVersionMapper, times(1)).deleteByPrimaryKey("id-123");
    }
}