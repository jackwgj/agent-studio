/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseObsConfigEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KnowledgeBaseObsConfigMapper {
    void insert(KnowledgeBaseObsConfigEntity knowledgeBaseIngestionConfigEntity);

    KnowledgeBaseObsConfigEntity selectByObsConfigId(@Param("id") String id);

    KnowledgeBaseObsConfigEntity selectByKnowledgeBaseId(@Param("knowledgeBaseId") String knowledgeBaseId);

    void updateObsTaskStatus(@Param("id") String id, @Param("taskStatus") String taskStatus);

    void deleteObsTaskConfig(@Param("knowledgeBaseId") String knowledgeBaseId);
}
