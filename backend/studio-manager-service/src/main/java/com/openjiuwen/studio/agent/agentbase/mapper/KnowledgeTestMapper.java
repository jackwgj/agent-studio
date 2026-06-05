/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeTestEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库命中测试操作mapper
 *
 * @since 2024-08-28
 */
@Mapper
public interface KnowledgeTestMapper {
    /**
     * 新增命中测试记录
     *
     * @param knowledgeTest KnowledgeTestEntity
     * @return number
     */
    int insert(KnowledgeTestEntity knowledgeTest);

    /**
     * 查询命中测试历史记录
     *
     * @param domainId 租户id
     * @param knowledgeRepoId 知识库id
     * @return 命中测试记录列表
     */
    List<KnowledgeTestEntity> selectByProjectIdAndRepoId(@Param("project_id") String projectId,
        @Param("knowledgeRepoId") String knowledgeRepoId);

    /**
     * 删除命中测试记录
     *
     * @param recordId 记录id
     * @param domainId 租户id
     * @return number
     */
    int deleteByPrimaryKey(@Param("recordId") String recordId, @Param("repoId") String repoId,
        @Param("project_id") String projectId);

    /**
     * 删除知识库所有命中测试记录
     *
     * @param domainId 租户id
     * @param knowledgeRepoId 知识库id
     * @return number
     */
    int deleteByRepoId(@Param("project_id") String projectId, @Param("knowledgeRepoId") String knowledgeRepoId);
}
