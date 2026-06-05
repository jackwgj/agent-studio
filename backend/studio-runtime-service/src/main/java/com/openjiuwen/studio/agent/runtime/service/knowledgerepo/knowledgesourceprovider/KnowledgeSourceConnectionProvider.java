/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.knowledgesourceprovider;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openjiuwen.studio.agent.common.enums.KnowledgeSourceEnum;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.runtime.enums.ConnectorTypeEnum;
import com.openjiuwen.studio.agent.runtime.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.KnowledgeSourceConnection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 知识库对接信息provider
 *
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public abstract class KnowledgeSourceConnectionProvider {

    private final KnowledgeBaseConnectionMapper knowledgeConnectionMapper;

    private final Cache<String, KnowledgeBaseConnectionEntity> knowledgeBaseConnectionEntityCacheByConnectionId
        = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(5, TimeUnit.MINUTES).build();

    private final Cache<String, KnowledgeBaseConnectionEntity> knowledgeBaseConnectionEntityCacheByRepoId
        = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(5, TimeUnit.MINUTES).build();

    private final Cache<String, KnowledgeBaseConnectionEntity> knowledgeBaseConnectionEntityCacheBySegmentId
        = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(5, TimeUnit.MINUTES).build();

    @Value("${knowledge.source}")
    private String knowledgeSource;

    /**
     * 获取知识库连接信息
     */
    public abstract KnowledgeSourceConnection getKnowledgeSourceConnection(String connectionId);

    protected KnowledgeBaseConnectionEntity getKnowledgeSourceConnectionByRepoId(String knowledgeBaseId) {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity
            = knowledgeBaseConnectionEntityCacheByRepoId.getIfPresent(knowledgeBaseId);
        if (knowledgeBaseConnectionEntity != null) {
            checkKnowledgeSourceCorrect(knowledgeBaseConnectionEntity);
            return knowledgeBaseConnectionEntity;
        }
        // 此处应查询数据库获取知识源连接信息
        knowledgeBaseConnectionEntity = knowledgeConnectionMapper.findById(knowledgeBaseId);
        if (knowledgeBaseConnectionEntity == null) {
            log.error("cannot find knowledge base connection info by knowledgeBaseId[{}]", knowledgeBaseId);
            throw new AgentStudioException(StudioError.FAILED_TO_FIND_DEFAULT_CONNECTION_INFO);
        }
        knowledgeBaseConnectionEntityCacheByRepoId.put(knowledgeBaseId, knowledgeBaseConnectionEntity);
        checkKnowledgeSourceCorrect(knowledgeBaseConnectionEntity);
        return knowledgeBaseConnectionEntity;
    }

    protected KnowledgeBaseConnectionEntity queryKnowledgeConnectionFromDb(String connectionId) {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity
            = knowledgeBaseConnectionEntityCacheByConnectionId.getIfPresent(connectionId);
        if (knowledgeBaseConnectionEntity != null) {
            checkKnowledgeSourceCorrect(knowledgeBaseConnectionEntity);
            return knowledgeBaseConnectionEntity;
        }
        // 此处应查询数据库获取知识源连接信息
        knowledgeBaseConnectionEntity = knowledgeConnectionMapper.selectOne(
            new LambdaQueryWrapper<KnowledgeBaseConnectionEntity>().eq(KnowledgeBaseConnectionEntity::getId,
                connectionId));
        if (knowledgeBaseConnectionEntity != null) {
            knowledgeBaseConnectionEntityCacheByConnectionId.put(connectionId, knowledgeBaseConnectionEntity);
        }
        checkKnowledgeSourceCorrect(knowledgeBaseConnectionEntity);
        return knowledgeBaseConnectionEntity;
    }

    private void checkKnowledgeSourceCorrect(KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity) {
        KnowledgeSourceEnum knowledgeSourceEnum = queryKnowledgeSourceEnum(knowledgeBaseConnectionEntity);
        if (KnowledgeSourceEnum.fromValue(knowledgeSource) != knowledgeSourceEnum) {
            log.error("knowledge source type[{}] is wrong", knowledgeSourceEnum);
            throw new AgentStudioException(StudioError.INVALID_KNOWLEDGE_REPO_SOURCE);
        }
    }

    private KnowledgeSourceEnum queryKnowledgeSourceEnum(KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity) {
        String connectorId = knowledgeBaseConnectionEntity.getConnectorId();
        ConnectorTypeEnum connectorTypeEnum = ConnectorTypeEnum.fromValue(connectorId);
        switch (Objects.requireNonNull(connectorTypeEnum)) {
            case KOO_SEARCH_INSIDE -> {
                return KnowledgeSourceEnum.KOOSEARCH;
            }
            case LAKE_SEARCH_INSIDE -> {
                return KnowledgeSourceEnum.LAKESEARCH;
            }
            case CUSTOM -> {
                return KnowledgeSourceEnum.CUSTOM;
            }
            default -> {
                log.error("inside connectorId[{}] is error", connectorId);
                throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
            }
        }
    }

    public String getConnectionIdByKnowledgeBaseId(String knowledgeBaseId) {
        return getKnowledgeSourceConnectionByRepoId(knowledgeBaseId).getId();
    }
}
