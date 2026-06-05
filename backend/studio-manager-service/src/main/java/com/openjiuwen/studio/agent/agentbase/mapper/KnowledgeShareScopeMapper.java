/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.mapper;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeShareScopeEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeShareScopeMapper {

    void batchInsert(@Param("knowledgeShareScope") List<KnowledgeShareScopeEntity> knowledgeShareScopeEntities);

    void deleteKnowledgeBase(@Param("projectId") String projectId, @Param("knowledgeBaseId") String knowledgeBaseId);

    KnowledgeShareScopeEntity queryShareScope(@Param("knowledgeBaseId") String knowledgeBaseId,
        @Param("workspaceId") String workspaceId);

    List<KnowledgeShareScopeEntity> findByKnowledgeBaseId(@Param("knowledgeBaseId") String knowledgeBaseId);

    void deleteByWorkspaceIdsAndKnowledgeBaseId(@Param("projectId") String projectId,
        @Param("knowledgeBaseId") String knowledgeBaseId, @Param("workspaceIdList") List<String> toDelete);

}