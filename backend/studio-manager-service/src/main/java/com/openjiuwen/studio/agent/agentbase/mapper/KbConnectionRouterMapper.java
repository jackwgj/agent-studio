/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import com.openjiuwen.studio.agent.agentbase.entity.KbConnectionRouterEntity;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseStatistics;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识文件分层规则数据库表操作类
 *
 * @since 2025-04-12
 */
@Mapper
public interface KbConnectionRouterMapper {

    List<KbConnectionRouterEntity> selectByTenantTypeAndDomainId(@Param("tenantType") String tenantType,
        @Param("domainId") String domainId);

    List<KnowledgeBaseStatistics> calculateRepoNumUnderEachConnection();

    void updateKnowledgebaseUsedNum(
        @Param("knowledgeBaseStatistics") List<KnowledgeBaseStatistics> knowledgeBaseStatistics);
}
