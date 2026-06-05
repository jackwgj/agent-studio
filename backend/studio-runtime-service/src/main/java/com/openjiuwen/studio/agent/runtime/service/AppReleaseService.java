/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.runtime.dto.ReleaseInfo;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * 应用网页发布获取releaseInfo
 *
 */
@Slf4j
@Validated
@RestController
public class AppReleaseService {
    @Autowired
    private RedisClient redisClient;

    @Value("${release.web-rel}")
    private String releaseWebRel;

    private static final long ONE_DAY_IN_MILLIS = 24L * 60 * 60 * 1000;

    private static final String VISIBILITY_SCOPE_TENANT = "TENANT";

    private static final int DEFAULT_CALL_COUNT = 100;

    private static final String LOCK_STR = "_lock";

    public @Nullable ReleaseInfo getReleaseInfo(String shortCode) {
        ReleaseInfo releaseInfo = new ReleaseInfo();
        String releaseKey = String.format(releaseWebRel, shortCode);
        try {
            if (redisClient.exists(releaseKey)) {
                releaseInfo = JSON.parseObject(redisClient.get(releaseKey), new TypeReference<>() { });
            }
        } catch (Exception exception) {
            log.error("Redisson failed to obtain the bucket. {}", exception.getMessage());
            throw new AgentStudioException(StudioError.REDISSON_GET_BUCKET_FAILED);
        }
        // 校验合法性
        checkReleaseInfo(releaseInfo, releaseKey);
        return releaseInfo;
    }

    private void checkReleaseInfo(ReleaseInfo releaseInfo, String releaseKey) {
        // 校验租户
        if (VISIBILITY_SCOPE_TENANT.equals(releaseInfo.getVisibilityScope())) {
            String requestProjectId = RequestContextUtils.getRequestProjectId();
            if (requestProjectId == null || !requestProjectId.equals(releaseInfo.getProjectId())) {
                log.error(
                    "Current tenant has no permission to execute, requestProjectId is {}, " + "releaseProjectId is {}",
                    requestProjectId, releaseInfo.getProjectId());
                throw new AgentStudioException(StudioError.PROJECT_ID_ERROR);
            }
        }
        // 校验次数和时间, 并兼容旧数据
        int alreadyBeenCalled = 0;
        if (releaseInfo.getAlreadyBeenCalled() != null) {
            alreadyBeenCalled = releaseInfo.getAlreadyBeenCalled() + 1;
        }
        Long nowTime = System.currentTimeMillis();
        Long oldTime = 0L;
        if (releaseInfo.getUpdateTime() != null) {
            oldTime = releaseInfo.getUpdateTime();
        }

        if (nowTime - oldTime > ONE_DAY_IN_MILLIS) {
            alreadyBeenCalled = 0;
        }

        int callCount = releaseInfo.getCallCount() == null ? DEFAULT_CALL_COUNT : releaseInfo.getCallCount();
        // callCount为-1，说明接口可不限调用次数
        if (callCount != -1 && alreadyBeenCalled > callCount) {
            log.error("The number of calls made on this day has reached the limit {}", releaseInfo.getCallCount());
            throw new AgentStudioException(StudioError.CALL_LIMIT_ERROR);
        }
        // 更新次数
        updateRedisReleaseInfo(releaseInfo, releaseKey, alreadyBeenCalled);
    }

    private void updateRedisReleaseInfo(ReleaseInfo releaseInfo, String releaseKey, Integer alreadyBeenCalled) {
        releaseInfo.setUpdateTime(System.currentTimeMillis());
        releaseInfo.setAlreadyBeenCalled(alreadyBeenCalled);
        RedisLock lock = null;
        try {
            lock = redisClient.getLock(releaseKey + LOCK_STR);
            // 获取redis分布式锁，如果锁不可用，则等待3秒
            if (lock.tryLock(Duration.ofSeconds(3))) {
                redisClient.set(releaseKey, com.alibaba.fastjson2.JSON.toJSONString(releaseInfo));
            } else {
                log.error("failed to get lock");
                throw new AgentStudioException(StudioError.REDISSON_GET_BUCKET_FAILED,
                    "other user is processing same redis key!");
            }
        } catch (Exception e) {
            log.error("redisson get bucket failed, other user is processing same redis key!", e);
            throw new AgentStudioException(StudioError.REDISSON_GET_BUCKET_FAILED);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }
}
