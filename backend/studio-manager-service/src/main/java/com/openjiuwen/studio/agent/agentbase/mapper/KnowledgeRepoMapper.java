/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeRepoSearchCriteria;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库 Mapper
 *
 * @since 2024-11-12
 */
@Mapper
public interface KnowledgeRepoMapper {
    int insert(KnowledgeRepoEntity knowledgeRepo);

    int deleteByPrimaryKey(@Param("knowledgeRepoId") String knowledgeRepoId, @Param("project_id") String projectId);

    int updateByPrimaryKeySelective(KnowledgeRepoEntity knowledgeRepo);

    KnowledgeRepoEntity selectByPrimaryKey(@Param("knowledgeRepoId") String knowledgeRepoId,
        @Param("project_id") String projectId);

    List<KnowledgeRepoEntity> selectByProjectIdAndSearchCriteria(@Param("project_id") String projectId,
        @Param("searchCriteria") KnowledgeRepoSearchCriteria searchCriteria);

    List<KnowledgeRepoEntity> selectByKnowledgeRepoIds(@Param("project_id") String projectId,
        @Param("knowledgeRepoIds") List<String> knowledgeRepoIds);

    List<KnowledgeRepoEntity> selectByKnowledgeRepoIdsAndWorkspaceId(@Param("project_id") String projectId,
        @Param("projectId") String workspaceId, @Param("knowledgeRepoIds") List<String> knowledgeRepoIds);

    /**
     * 原子化更新知识库大小，避免先查后更新在高并发下的不准确
     *
     * @param knowledgeRepoId 知识库id
     * @param projectId 租户项目id
     * @param changedSize 知识库变化的大小，上传文件时为正，删除文件时为负
     * @return int
     */
    int atomicUpdateRepoSize(@Param("knowledgeRepoId") String knowledgeRepoId, @Param("project_id") String projectId,
        @Param("changedSize") long changedSize, @Param("changeFileNums") int changeFileNums);

    Long sumKnowledgeRepoSizeWithTenantType(@Param("domainId") String domainId, @Param("tenantType") String tenantType);
}
