/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeSourceConnection;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.common.enums.KnowledgeSourceEnum;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.connection.constants.ConnectorTypeEnum;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    protected KnowledgeSourceConnectionProvider(KnowledgeBaseConnectionMapper knowledgeConnectionMapper) {
        this.knowledgeConnectionMapper = knowledgeConnectionMapper;
    }

    /**
     * 获取知识库连接信息
     */
    public abstract KnowledgeSourceConnection getKnowledgeSourceConnection(String connectionId);

    protected KnowledgeBaseConnectionEntity getKnowledgeSourceConnectionByRepoId(String repoId) {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity
            = knowledgeBaseConnectionEntityCacheByRepoId.getIfPresent(repoId);
        if (knowledgeBaseConnectionEntity != null) {
            checkKnowledgeSourceCorrect(knowledgeBaseConnectionEntity);
            return knowledgeBaseConnectionEntity;
        }
        // 此处应查询数据库获取知识源连接信息
        knowledgeBaseConnectionEntity = knowledgeConnectionMapper.findByExternalRepoId(repoId);
        if (knowledgeBaseConnectionEntity == null) {
            log.error("cannot find knowledge base connection info by repoId[{0}]", repoId);
            throw new AgentBaseException(ErrorCode.FAILED_TO_FIND_DEFAULT_CONNECTION_INFO);
        }
        knowledgeBaseConnectionEntityCacheByRepoId.put(repoId, knowledgeBaseConnectionEntity);
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
        knowledgeBaseConnectionEntity = knowledgeConnectionMapper.findById(connectionId);
        if (knowledgeBaseConnectionEntity != null) {
            knowledgeBaseConnectionEntityCacheByConnectionId.put(connectionId, knowledgeBaseConnectionEntity);
        }
        checkKnowledgeSourceCorrect(knowledgeBaseConnectionEntity);
        return knowledgeBaseConnectionEntity;
    }

    protected KnowledgeBaseConnectionEntity getKnowledgeSourceConnectionBySegmentId(String segmentId) {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity
            = knowledgeBaseConnectionEntityCacheBySegmentId.getIfPresent(segmentId);
        if (knowledgeBaseConnectionEntity != null) {
            checkKnowledgeSourceCorrect(knowledgeBaseConnectionEntity);
            return knowledgeBaseConnectionEntity;
        }
        // 此处应查询数据库获取知识源连接信息
        knowledgeBaseConnectionEntity = knowledgeConnectionMapper.findConnectionBySegmentId(segmentId);
        if (knowledgeBaseConnectionEntity == null) {
            log.error("cannot find knowledge base connection info by segmentId[{0}]", segmentId);
            throw new AgentBaseException(ErrorCode.FAILED_TO_FIND_DEFAULT_CONNECTION_INFO);
        }
        knowledgeBaseConnectionEntityCacheByRepoId.put(segmentId, knowledgeBaseConnectionEntity);
        checkKnowledgeSourceCorrect(knowledgeBaseConnectionEntity);
        return knowledgeBaseConnectionEntity;
    }

    private void checkKnowledgeSourceCorrect(KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity) {
        KnowledgeSourceEnum knowledgeSourceEnum = queryKnowledgeSourceEnum(knowledgeBaseConnectionEntity);
        if (KnowledgeSourceEnum.fromValue(knowledgeSource) != knowledgeSourceEnum) {
            log.error("knowledge source type[{0}] is wrong", knowledgeSourceEnum);
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_SOURCE_TYPE_IS_WRONG);
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
                log.error("inside connectorId[{0}] is error", connectorId);
                throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR);
            }
        }
    }

    public String getConnectionIdByRepoId(String repoId) {
        return getKnowledgeSourceConnectionByRepoId(repoId).getId();
    }
}
