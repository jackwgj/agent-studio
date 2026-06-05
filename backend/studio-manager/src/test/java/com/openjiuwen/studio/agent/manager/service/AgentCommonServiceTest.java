/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.entity.HistoryReleaseVersionEntity;
import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.repository.HistoryReleaseVersionRepository;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

/**
 * AgentCommonServiceTest
 */
public class AgentCommonServiceTest extends BaseTest {

    @Mock
    private ReleaseVersionMapper releaseVersionMapper;

    @Mock
    private HistoryReleaseVersionRepository historyReleaseVersionRepository;

    @InjectMocks
    private AgentCommonService agentCommonService;

    @Test
    public void test_softDeleteReleaseVersionByAppId_not_exist() throws Exception {
        ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
        releaseVersion.setVersionId("v1.0");
        releaseVersion.setAppId("testAppId");
        when(releaseVersionMapper.selectByAppId(anyString())).thenReturn(List.of(releaseVersion));
        when(releaseVersionMapper.deleteByAppId(anyString())).thenReturn(0);
        when(historyReleaseVersionRepository.findByAppIdAndVersionId("testAppId", "v1.0")).thenReturn(
            Optional.of(mock(HistoryReleaseVersionEntity.class)));
        agentCommonService.softDeleteReleaseVersionByAppId("testAppId");
    }
}
