/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.memory;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.UserProfileMemoryConfig;
import com.openjiuwen.studio.agent.manager.dto.UserProfileTagInfo;
import com.openjiuwen.studio.agent.manager.dto.UserProfileTopicInfo;
import com.openjiuwen.studio.agent.manager.service.AgentCommonService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Agent记忆相关的配置服务
 *
 */
@Service
@Slf4j
public class AgentMemoryConfigService {

    private final AgentCommonService agentCommonService;

    @Value("${memory.user-profile-enable:false}")
    private boolean userProfileEnable;

    public AgentMemoryConfigService(AgentCommonService agentCommonService) {
        this.agentCommonService = agentCommonService;
    }

    /**
     * 检查同一个App内是否已经存在相同的标签
     *
     * @param userProfileMemoryConfig
     */
    public void checkUserProfileConfig(UserProfileMemoryConfig userProfileMemoryConfig) {
        if (!userProfileEnable) {
            return;
        }
        if (userProfileMemoryConfig == null || CollectionUtils.isEmpty(userProfileMemoryConfig.getTopics())) {
            return;
        }
        Map<String, Integer> topicMap = new HashMap<>();
        Set<String> emptyTopics = new HashSet<>();
        Map<String, Integer> tagMaps = new HashMap<>();
        for (UserProfileTopicInfo topic : userProfileMemoryConfig.getTopics()) {
            int topicCount = topicMap.getOrDefault(topic.getTopicName(), 0);
            topicCount++;
            topicMap.put(topic.getTopicName(), topicCount);
            if (CollectionUtils.isEmpty(topic.getTags())) {
                emptyTopics.add(topic.getTopicName());
                continue;
            }
            for (UserProfileTagInfo tag : topic.getTags()) {
                String fullName = joinTopicAndTagName(topic.getTopicName(), tag.getTagName());
                int tagCount = tagMaps.getOrDefault(fullName, 0);
                tagCount++;
                tagMaps.put(fullName, tagCount);
            }
        }
        // 同一个App内主题不能重复
        List<String> duplicateTopics = topicMap.entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Map.Entry::getKey)
            .toList();
        if (CollectionUtils.isNotEmpty(duplicateTopics)) {
            throw new AgentStudioException(StudioError.PROFILE_TOPIC_DUPLICATE, String.join(",", duplicateTopics));
        }
        // 主题下必须有标签
        if (CollectionUtils.isNotEmpty(emptyTopics)) {
            throw new AgentStudioException(StudioError.EMPTY_TOPIC, String.join(",", emptyTopics));
        }
        // 同一主题下标签不能重复
        List<String> duplicateTags = tagMaps.entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Map.Entry::getKey)
            .toList();
        if (CollectionUtils.isNotEmpty(duplicateTags)) {
            throw new AgentStudioException(StudioError.PROFILE_TAG_DUPLICATE, String.join(",", duplicateTags));
        }
    }

    /**
     * 存储记忆的定义（v4.2: scope config 由 Python 侧在首次记忆提取时懒创建，不再同步到 memory-service）
     *
     * @param projectId
     * @param agentId
     * @param newUserProfileMemoryConfig
     * @param oldUserProfileMemoryConfig
     */
    public void saveUserProfileConfigs(String projectId, String agentId,
        UserProfileMemoryConfig newUserProfileMemoryConfig, UserProfileMemoryConfig oldUserProfileMemoryConfig) {
        if (!userProfileEnable) {
            return;
        }
        // v4.2: No longer sync to memory-service. Scope config is lazily created by Python
        // agent-runtime during first memory extraction, driven by IR strategies.
        if (newUserProfileMemoryConfig != null && CollectionUtils.isNotEmpty(newUserProfileMemoryConfig.getTopics())) {
            log.info("User profile config saved for agent {} ({} topics) — scope config will be lazily created by runtime",
                agentId, newUserProfileMemoryConfig.getTopics().size());
        }
    }

    /**
     * 删除某个应用中的所有用户长期记忆（v4.2: 不再通过 memory-service，改为 Python LTM 内部端点处理）
     *
     * @param iamToken
     * @param language
     * @param projectId
     * @param appId
     */
    public void clearUserMemoriesInApplication(String iamToken, String language, String projectId, String appId) {
        if (!userProfileEnable) {
            return;
        }
        // v4.2: Memory clearing is handled by MemoryRepoInternalController → JiuWenClient → Python LTM.
        // This method is retained as a compatibility no-op.
        log.info("clearUserMemoriesInApplication called for app {} — handled by LTM internal endpoints", appId);
    }

    private String joinTopicAndTagName(String topicName, String tagName) {
        return topicName + ":" + tagName;
    }
}
