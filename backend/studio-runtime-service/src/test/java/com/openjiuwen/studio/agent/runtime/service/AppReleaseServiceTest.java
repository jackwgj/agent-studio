/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service;

import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.runtime.dto.ReleaseInfo;
import com.openjiuwen.studio.agent.runtime.mapper.ChannelReleaseMapper;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

/**
 * 功能描述
 *
 */
@Slf4j
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppReleaseServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelReleaseMapper channelReleaseMapper;

    @Mock
    private RedisClient redisClient;

    @InjectMocks
    private AppReleaseService appReleaseService;

    @Test
    void test_getReleaseInfo_all() throws Exception {
        ReflectionTestUtils.setField(appReleaseService, "releaseWebRel", "release-web-rel-%s");
        String shortCode = "testShortCode";
        String releaseKey = String.format("release-web-rel-%s", shortCode);
        String releaseInfoJson
            = "{\"app_id\":\"testAppId\",\"app_type\":\"testAppType\",\"version_id\":\"testVersionId\",\"channel_type\":\"testChannelType\",\"short_code\":\"testShortCode\",\"project_id\":\"testProjectId\",\"workspace_id\":\"testWorkspaceId\",\"already_been_called\":0,\"visibility_scope\":\"ALL\",\"call_count\":10,\"update_time\":1633072800000}";

        when(redisClient.exists(releaseKey)).thenReturn(true);
        when(redisClient.get(releaseKey)).thenReturn(releaseInfoJson);
        RedisLock lock = Mockito.mock(RedisLock.class);
        when(redisClient.getLock(releaseKey + "_lock")).thenReturn(lock);
        when(lock.tryLock(Duration.ofSeconds(3))).thenReturn(true);
        ReleaseInfo releaseInfo = appReleaseService.getReleaseInfo(shortCode);
    }

    @Test
    void test_getReleaseInfo_tenant() throws Exception {
        // 设置字段值
        ReflectionTestUtils.setField(appReleaseService, "releaseWebRel", "release-web-rel-%s");

        String shortCode = "testShortCode";
        String releaseKey = String.format("release-web-rel-%s", shortCode);
        String releaseInfoJson
            = "{\"app_id\":\"testAppId\",\"app_type\":\"testAppType\",\"version_id\":\"testVersionId\",\"channel_type\":\"testChannelType\",\"short_code\":\"testShortCode\",\"project_id\":\"testProjectId\",\"workspace_id\":\"testWorkspaceId\",\"already_been_called\":0,\"visibility_scope\":\"TENANT\",\"call_count\":10,\"update_time\":1633072800000}";

        try (MockedStatic<RequestContextUtils> mockedRequestContextUtils = Mockito.mockStatic(
            RequestContextUtils.class)) {
            mockedRequestContextUtils.when(RequestContextUtils::getRequestProjectId).thenReturn(null);

            when(redisClient.exists(releaseKey)).thenReturn(true);
            when(redisClient.get(releaseKey)).thenReturn(releaseInfoJson);
            RedisLock lock = Mockito.mock(RedisLock.class);
            when(redisClient.getLock(releaseKey)).thenReturn(lock);
            when(lock.tryLock(Duration.ofSeconds(3))).thenReturn(true);

            try {
                ReleaseInfo releaseInfo = appReleaseService.getReleaseInfo(shortCode);
            } catch (Exception e) {
                log.error("Current tenant has no permission to execute");
            }
        }
    }

    @Test
    void test_getReleaseInfo_call_count() throws Exception {
        ReflectionTestUtils.setField(appReleaseService, "releaseWebRel", "release-web-rel-%s");
        String shortCode = "testShortCode";
        String releaseKey = String.format("release-web-rel-%s", shortCode);
        Long nowTime = System.currentTimeMillis();
        String releaseInfoJson
            = "{\"app_id\":\"testAppId\",\"app_type\":\"testAppType\",\"version_id\":\"testVersionId\",\"channel_type\":\"testChannelType\",\"short_code\":\"testShortCode\",\"project_id\":\"testProjectId\",\"workspace_id\":\"testWorkspaceId\",\"already_been_called\":100,\"visibility_scope\":\"ALL\",\"call_count\":100,\"update_time\":1762172816740}";

        when(redisClient.exists(releaseKey)).thenReturn(true);
        when(redisClient.get(releaseKey)).thenReturn(releaseInfoJson);
        RedisLock lock = Mockito.mock(RedisLock.class);
        when(redisClient.getLock(releaseKey)).thenReturn(lock);
        when(lock.tryLock(Duration.ofSeconds(3))).thenReturn(true);

        try {
            ReleaseInfo releaseInfo = appReleaseService.getReleaseInfo(shortCode);
        } catch (Exception e) {
            log.error("The number of calls made on this day has reached the limit");
        }
    }
}
