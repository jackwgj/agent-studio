/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.memory;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.runtime.model.memory.NaturalLanguageMemory;
import com.openjiuwen.studio.agent.runtime.model.memory.LongTermMemoryRefreshEvent;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户记忆 缓冲区管理Service
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserMemoryCacheMgmtService {
    private final RedisClient redisClient;

    private static final String USER_AGENT_MEMORY_REFRESHED_EVENT_KEY_FORMAT
        = "agent.service.user.memory.refresh.event.%s.%s.%s";

    /**
     * getStoredLongTermMemoryRefreshEvent
     *
     * @param userId         userId
     * @param memoryRepoId  memoryRepoId
     * @param conversationId conversationId
     * @return UserProfileRefreshEvent
     */
    public LongTermMemoryRefreshEvent getStoredLongTermMemoryRefreshEvent(String userId, String memoryRepoId,
        String conversationId) {
        if (StringUtils.isAnyBlank(userId, memoryRepoId, conversationId)) {
            log.info("Blank userId, memoryRepoId or conversationId, can not get stored memory refresh event.");
            return null;
        }

        log.info("Get stored memory refresh event for user {}, memoryRepoId {}, conversationId {}.", userId,
            memoryRepoId, conversationId);
        String storedMemoryRefreshEventStr = redisClient.get(
            obtainUserMemoryRefreshEventKey(userId, memoryRepoId, conversationId));
        if (StringUtils.isBlank(storedMemoryRefreshEventStr)) {
            log.info("Stored memory refresh event is blank. Got null event.");
            return null;
        }

        LongTermMemoryRefreshEvent memoryRefreshEvent = JsonUtils.json2ObjQuietly(storedMemoryRefreshEventStr,
            LongTermMemoryRefreshEvent.class);

        // 记忆更新消息中可能存在一些重复的消息，把这些重复的消息去掉
        List<NaturalLanguageMemory> memories = Optional.ofNullable(memoryRefreshEvent)
            .map(LongTermMemoryRefreshEvent::getData)
            .map(LongTermMemoryRefreshEvent.EventData::getMemories)
            .orElse(Collections.emptyList());
        // 如果列表中的元素重复，则去除对应的元素
        if (CollectionUtils.isNotEmpty(memories)) {
            Set<NaturalLanguageMemory> uniqueMemories = new HashSet<>(memories);
            memoryRefreshEvent.getData().setMemories(new ArrayList<>(uniqueMemories));
        }
        return memoryRefreshEvent;
    }

    /**
     * deleteStoredLongTermMemoryRefreshEvent
     *
     * @param userId         userId
     * @param memoryRepoId  memoryRepoId
     * @param conversationId conversationId
     */
    public void deleteStoredLongTermMemoryRefreshEvent(String userId, String memoryRepoId, String conversationId) {
        if (StringUtils.isAnyBlank(userId, memoryRepoId, conversationId)) {
            log.info(
                "Blank userId, memoryRepoId or conversationId. Failed to delete stored user profile refresh event.");
            return;
        }
        redisClient.delete(obtainUserMemoryRefreshEventKey(userId, memoryRepoId, conversationId));
        log.info("Delete stored user profile refresh event. UserId: {}, memoryRepoId: {}, conversationId: {}.", userId,
            memoryRepoId, conversationId);
    }

    private String obtainUserMemoryRefreshEventKey(String userId, String memoryRepoId, String conversationId) {
        return String.format(USER_AGENT_MEMORY_REFRESHED_EVENT_KEY_FORMAT, userId, memoryRepoId, conversationId);
    }
}
