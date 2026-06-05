/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseObsExecuteRecordEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeBaseObsExecuteRecordMapper {
    KnowledgeBaseObsExecuteRecordEntity selectRecordById(@Param("id") String id);

    Integer insertExecuteRecord(KnowledgeBaseObsExecuteRecordEntity knowledgeBaseObsExecuteRecordEntity);

    Integer updateExecuteRecordById(KnowledgeBaseObsExecuteRecordEntity knowledgeBaseObsExecuteRecordEntity);

    List<KnowledgeBaseObsExecuteRecordEntity> selectListByObsConfigId(@Param("obsConfigId") String obsConfigId);

    void deleteExecuteRecordByObsConfigId(@Param("obsConfigId") String obsConfigId);
}
