/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class PluginCleanTaskTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PluginMapper pluginMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReleaseVersionMapper releaseVersionMapper;

    @InjectMocks
    private PluginCleanTask pluginCleanTask;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(pluginCleanTask, "lastSuccessfulPluginCleanTime", LocalDateTime.now());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }


    @Test
    void test_cleanOldSoftDeletePluginIds_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(pluginMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);
            List<String> pluginIds = new ArrayList<>();
            pluginIds.add("not_empty");
            when(pluginMapper.findPluginToBeDeleted(any(LocalDateTime.class))).thenReturn(pluginIds);

            List<ReleaseVersion> releaseVersions = new ArrayList<>();
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersions.add(releaseVersion);
            when(releaseVersionMapper.selectByAppId(anyString())).thenReturn(releaseVersions);

            // When
            pluginCleanTask.cleanOldSoftDeletePluginIds();
        });
    }

    @Test
    void test_cleanOldSoftDeletePluginIds_should_not_throw_exception2() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(pluginMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);
            when(pluginMapper.findPluginToBeDeleted(any(LocalDateTime.class))).thenReturn(null);

            when(releaseVersionMapper.selectByAppId(anyString())).thenReturn(null);

            // When
            pluginCleanTask.cleanOldSoftDeletePluginIds();
        });
    }

    @Test
    void testGetTaskStatus() {
        // Arrange
        pluginCleanTask.lastSuccessfulPluginCleanTime = LocalDateTime.now().minusHours(1);
        pluginCleanTask.lastSuccessfulPluginCleanCount = 3;

        // Act
        String status = pluginCleanTask.getTaskStatus();

        // Assert
        assertTrue(status.contains("lastPluginIdSuccess"));
        assertTrue(status.contains("pluginIdsToClean = 3"));
        assertTrue(status.contains("PluginCleanTask["));
    }
}
