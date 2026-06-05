/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.memory;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.UserProfileMemoryConfig;
import com.openjiuwen.studio.agent.manager.dto.UserProfileTagInfo;
import com.openjiuwen.studio.agent.manager.dto.UserProfileTopicInfo;
import com.openjiuwen.studio.agent.manager.rce.client.MemoryServiceClient;
import com.openjiuwen.studio.agent.manager.rce.models.memory.UserProfileDeleteRequest;
import com.openjiuwen.studio.agent.manager.rce.models.memory.UserProfileModifyRequest;
import com.openjiuwen.studio.agent.manager.rce.models.memory.UserProfileTag;
import com.openjiuwen.studio.agent.manager.rce.models.memory.UserProfileTopic;
import com.openjiuwen.studio.agent.manager.service.AgentCommonService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final MemoryServiceClient memoryServiceClient;

    private final ExecutorService memoryOperationExecutor;

    public AgentMemoryConfigService(MemoryServiceClient memoryServiceClient, AgentCommonService agentCommonService) {
        this.memoryServiceClient = memoryServiceClient;
        this.agentCommonService = agentCommonService;
        this.memoryOperationExecutor = Executors.newFixedThreadPool(2);
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
     * 存储记忆的定义
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
        if (newUserProfileMemoryConfig == null) {
            // 为null时，表示不更新用户画像记忆
            return;
        }
        List<UserProfileTopic> topicsToModify = new ArrayList<>();
        Set<String> newTags = new HashSet<>();
        for (UserProfileTopicInfo topic : ObjectUtils.firstNonNull(newUserProfileMemoryConfig.getTopics(),
            Collections.<UserProfileTopicInfo>emptyList())) {
            UserProfileTopic topicConfig = new UserProfileTopic();
            topicConfig.setTopic(topic.getTopicName());
            topicConfig.setTags(new ArrayList<>());
            topicsToModify.add(topicConfig);
            for (UserProfileTagInfo tag : ObjectUtils.firstNonNull(topic.getTags(),
                Collections.<UserProfileTagInfo>emptyList())) {
                UserProfileTag newTag = UserProfileTag.builder()
                    .name(tag.getTagName())
                    .description(tag.getTagDescription())
                    .build();
                topicConfig.getTags().add(newTag);
                newTags.add(joinTopicAndTagName(topic.getTopicName(), tag.getTagName()));
            }
        }

        List<UserProfileTopic> topicsToDelete = new ArrayList<>();
        for (UserProfileTopicInfo oldTopic : Optional.ofNullable(oldUserProfileMemoryConfig)
            .map(UserProfileMemoryConfig::getTopics)
            .orElse(Collections.<UserProfileTopicInfo>emptyList())) {
            UserProfileTopic topicToDelete = new UserProfileTopic();
            topicToDelete.setTopic(oldTopic.getTopicName());
            topicToDelete.setTags(new ArrayList<>());
            for (UserProfileTagInfo oldTag : ObjectUtils.firstNonNull(oldTopic.getTags(),
                Collections.<UserProfileTagInfo>emptyList())) {
                if (!newTags.contains(joinTopicAndTagName(oldTopic.getTopicName(), oldTag.getTagName()))) {
                    UserProfileTag tagToDelete = new UserProfileTag();
                    tagToDelete.setName(oldTag.getTagName());
                    topicToDelete.getTags().add(tagToDelete);
                }
            }
            if (CollectionUtils.isNotEmpty(topicToDelete.getTags())) {
                topicsToDelete.add(topicToDelete);
            }
        }

        UserProfileModifyRequest request = UserProfileModifyRequest.builder().topics(topicsToModify).build();
        // 需要存到memory-server中一份，让九问的记忆引擎感知有哪些用户画像记忆
        memoryServiceClient.modifyUserProfileConfig(RequestContextUtils.getRequestAuthToken(),
            LanguageUtils.getLanguage(), projectId, request);
        log.info("modify {} topics from memory-service in agent {}.", topicsToModify.size(), agentId);
        // 如果有需要删除的记忆，也需要让九问的记忆引擎感知有哪些用户画像记忆被删除了
        if (CollectionUtils.isNotEmpty(topicsToDelete)) {
            UserProfileDeleteRequest deleteRequest = UserProfileDeleteRequest.builder().topics(topicsToDelete).build();
            log.info("delete {} topics from memory-service in agent {}.", topicsToDelete.size(), agentId);
            memoryServiceClient.deleteUserProfileConfig(RequestContextUtils.getRequestAuthToken(),
                LanguageUtils.getLanguage(), projectId, deleteRequest);
        }
    }

    /**
     * 删除某个应用中的所有用户长期记忆
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
        memoryOperationExecutor.submit(() -> {
            // 删除长期记忆
            try {
                memoryServiceClient.deleteLongTermMemories(iamToken, language, projectId, appId);
                log.info("Successfully cleared user long term memories for application: {}", appId);
            } catch (Exception e) {
                log.error("Failed to clear user long term memories for application: {}", appId, e);
            }
        });
    }

    private String joinTopicAndTagName(String topicName, String tagName) {
        return topicName + ":" + tagName;
    }
}
