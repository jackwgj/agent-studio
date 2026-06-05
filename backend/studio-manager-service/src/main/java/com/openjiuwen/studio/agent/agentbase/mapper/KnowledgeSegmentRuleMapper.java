/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeSegmentRuleEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识文件分层规则数据库表操作类
 *
 * @since 2025-04-12
 */
@Mapper
public interface KnowledgeSegmentRuleMapper {
    int insert(KnowledgeSegmentRuleEntity segmentRule);

    int deleteByPrimaryKey(@Param("id") String segmentRuleId, @Param("project_id") String projectId);

    int updateByPrimaryKeySelective(KnowledgeSegmentRuleEntity segmentRule);

    KnowledgeSegmentRuleEntity selectByPrimaryKey(@Param("id") String segmentRuleId,
        @Param("project_id") String projectId);

    List<KnowledgeSegmentRuleEntity> selectByDomainId(@Param("project_id") String projectId,
        @Param("workspaceId") String workspaceId, @Param("connectionId") String connectionId);

    KnowledgeSegmentRuleEntity selectByIdAndWorkspaceId(@Param("workspaceId") String workspaceId,
        @Param("id") String id);
}
