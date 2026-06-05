/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.sensitive;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.openjiuwen.studio.agent.common.bo.AgentMetadata;
import com.openjiuwen.studio.agent.common.sensitive.SensitiveEntity;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.model.AgentIrWrapper;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrWrapper;
import com.openjiuwen.studio.agent.runtime.service.AgentIrCacheService;
import com.openjiuwen.studio.agent.runtime.service.WorkflowIrCacheService;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * 敏感词树缓存管理类
 *
 */
@Component
@Slf4j
public class SensitiveTrieCache {
    private static final String CONCAT_STRING = "_";

    private static final int MAX_CACHE_SIZE = 1000;

    private LoadingCache<ComposedCacheKey, SensitiveTrieWrapper> trieWrapperCache;

    private final WorkflowIrCacheService workflowIrService;

    private final AgentIrCacheService agentIrCacheService;

    public SensitiveTrieCache(WorkflowIrCacheService workflowIrService, AgentIrCacheService agentIrCacheService) {
        this.workflowIrService = workflowIrService;
        this.agentIrCacheService = agentIrCacheService;
    }

    @PostConstruct
    public void init() {
        trieWrapperCache =
            Caffeine.newBuilder().maximumSize(MAX_CACHE_SIZE).expireAfterAccess(1, TimeUnit.HOURS).build(cacheKey -> {
                log.info("[sensitive]: start to build trie cache, appType:{}, appId:{}, appVersion:{}",
                    cacheKey.appType, cacheKey.appId, cacheKey.releasedVersion);
                // 从 DSL 下载敏感词配置
                long updateAt;
                SensitiveEntity entity;
                if (Constant.AppType.WORKFLOW.equals(cacheKey.appType)) {
                    WorkflowIrWrapper irWrapper =
                        workflowIrService.getWorkflowIr(cacheKey.appId, cacheKey.releasedVersion);
                    entity = irWrapper.getSensitiveEntity();
                    updateAt = irWrapper.getMetadata().getUpdatedAt();
                } else {
                    AgentIrWrapper irWrapper;
                    if (StringUtils.isBlank(cacheKey.releasedVersion)) {
                        irWrapper = agentIrCacheService.getLatestAgentIr(cacheKey.appId);
                    } else {
                        irWrapper = agentIrCacheService.getPublishedAgentIr(cacheKey.appId, cacheKey.releasedVersion);
                    }
                    entity = irWrapper.getSensitiveEntity();
                    updateAt = irWrapper.getMetadata().getUpdatedAt().getTime();
                }
                if (entity == null) {
                    return SensitiveTrieWrapper.builder().isEnabled(false).build();
                }
                // 构建敏感词字典树
                SensitiveTrie replyTrie = new SensitiveTrie();
                SensitiveTrie mixedTrie = new SensitiveTrie();
                processReply(mixedTrie, replyTrie, entity);
                processFilterAndReplace(mixedTrie, entity);
                return SensitiveTrieWrapper.builder()
                    .isEnabled(entity.isEnabled())
                    .updateAt(updateAt)
                    .inputReplyTrie(replyTrie)
                    .outputMixedTrie(mixedTrie)
                    .build();
            });
    }

    /**
     * 获取敏感词字典树包装类
     *
     * @param appType app 类型
     * @param metadata app 版本信息
     * @return 敏感词包装类
     */
    public SensitiveTrieWrapper getSensitiveTrieWrapper(String appType, AgentMetadata metadata) {
        ComposedCacheKey cacheKey = new ComposedCacheKey(appType, metadata.getAgentId(), metadata.getVersionId());
        SensitiveTrieWrapper trieWrapper = trieWrapperCache.get(cacheKey);
        // 缓存刷新
        if (trieWrapper.updateAt != metadata.getUpdatedAt()) {
            trieWrapperCache.invalidate(cacheKey);
            trieWrapper = trieWrapperCache.get(cacheKey);
        }
        return trieWrapper;
    }

    private void processReply(SensitiveTrie mixedTrie, SensitiveTrie replyTrie, SensitiveEntity entity) {
        if (entity.getReply() != null) {
            for (SensitiveEntity.WordsWrapper wrapper : entity.getReply()) {
                if (!wrapper.isInputEnable() && !wrapper.isOutputEnable()) {
                    continue;
                }
                List<String> keywords = wrapper.getKeywordsList();
                keywords.forEach(word -> {
                    if (wrapper.isInputEnable() && StringUtils.isNotEmpty(wrapper.getInputReplyText())) {
                        replyTrie.insert(word, SensitiveTrie.SensitiveOp.REPLY, wrapper.getInputReplyText());
                    }
                    if (wrapper.isOutputEnable() && StringUtils.isNotEmpty(wrapper.getOutputReplyText())) {
                        mixedTrie.insert(word, SensitiveTrie.SensitiveOp.REPLY, wrapper.getOutputReplyText());
                    }
                });
            }
        }
    }

    private void processFilterAndReplace(SensitiveTrie mixedTrie, SensitiveEntity entity) {
        // 替换
        if (entity.getReplace() != null) {
            for (SensitiveEntity.WordsWrapper wrapper : entity.getReplace()) {
                if (StringUtils.isNotEmpty(wrapper.getReplace())) {
                    List<String> keywords = wrapper.getKeywordsList();
                    keywords.forEach(word -> {
                        mixedTrie.insert(word, SensitiveTrie.SensitiveOp.REPLACE, wrapper.getReplace());
                    });
                }
            }
        }
        // 过滤
        if (entity.getFilter() != null) {
            List<String> keywords = entity.getFilter().getKeywordsList();
            keywords.forEach(word -> {
                mixedTrie.insert(word, SensitiveTrie.SensitiveOp.FILTER, "");
            });
        }
    }

    /**
     * 缓存 key 包装类
     *
     * @param appType app 类型
     * @param appId app id
     * @param releasedVersion app 发布版本
     */
    private record ComposedCacheKey(String appType, String appId, String releasedVersion) {
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ComposedCacheKey that = (ComposedCacheKey) o;
            return Strings.CS.equals(appType, that.appType) && Strings.CS.equals(appId, that.appId)
                && Strings.CS.equals(releasedVersion, that.releasedVersion);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + (appType != null ? appType.hashCode() : 0);
            result = 31 * result + (appId != null ? appId.hashCode() : 0);
            result = 31 * result + (releasedVersion != null ? releasedVersion.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            if (StringUtils.isEmpty(releasedVersion)) {
                return appId;
            } else {
                return appId + CONCAT_STRING + releasedVersion;
            }
        }
    }

    /**
     * 敏感词树包装类
     *
     */
    @Builder
    static class SensitiveTrieWrapper {
        boolean isEnabled;

        long updateAt;

        SensitiveTrie inputReplyTrie;

        SensitiveTrie outputMixedTrie;
    }
}
