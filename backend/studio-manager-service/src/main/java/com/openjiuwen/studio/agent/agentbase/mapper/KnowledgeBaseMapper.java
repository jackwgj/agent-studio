/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import com.github.pagehelper.PageRowBounds;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.enums.ShareScopeEnum;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper {

    /**
     * 查询知识库列表
     *
     * @param projectId
     * @param workspaceId
     * @param name 按照名称模糊查询
     * @param fullName 按照名称精确查询
     * @param type
     * @param status
     * @param connectionId
     * @param createUserId
     * @param knowledgeBaseIds
     * @param pageRowBounds
     * @return
     */
    List<KnowledgeBaseEntity> queryKnowledgeBaseList(@Param("project_id") String projectId,
        @Param("workspaceId") String workspaceId, @Param("shareScope") String shareScope, @Param("name") String name,
        @Param("fullName") String fullName, @Param("type") String type, @Param("status") String status,
        @Param("connectionId") String connectionId, @Param("createUserId") String createUserId,
        @Param("knowledgeBaseIds") List<String> knowledgeBaseIds, PageRowBounds pageRowBounds);

    List<KnowledgeBaseEntity> queryKnowledgeBaseListByShareScope(@Param("project_id") String projectId,
        @Param("name") String name, @Param("fullName") String fullName, @Param("type") String type,
        @Param("status") String status, @Param("shareScope") String shareScope,
        @Param("connectionId") String connectionId, @Param("createUserId") String createUserId,
        @Param("knowledgeBaseIds") List<String> knowledgeBaseIds, PageRowBounds pageRowBounds);

    Integer insertKnowledgeBase(KnowledgeBaseEntity knowledgeBaseEntity);

    Integer updateKnowledgeBase(@Param("project_id") String projectId,
        @Param("knowledgeBase") KnowledgeBaseEntity knowledgeBaseEntity);

    Integer batchDeleteKnowledgeBase(@Param("project_id") String projectId,
        @Param("knowledgeBaseIds") List<String> knowledgeBaseIds);

    KnowledgeBaseEntity selectById(@Param("project_id") String projectId, @Param("id") String knowledgeBaseId);

    KnowledgeBaseEntity selectByIdAndWorkspaceId(@Param("workspaceId") String workspaceId,
        @Param("id") String knowledgeBaseId);

    List<KnowledgeBaseEntity> batchQueryKnowledgeBases(@Param("project_id") String projectId,
        @Param("workspaceId") String workSpaceId, @Param("ids") List<String> ids,
        @Param("externalIds") List<String> externalIds, @Param("copySourceIds") List<String> copySourceIds);

    Integer batchInsertKnowledgeBase(@Param("knowledgeBaseList") List<KnowledgeBaseEntity> knowledgeBaseEntityList);

    List<String> countByType(@Param("type") String type, @Param("domain_id") String domainId);

    Integer updatePreviewsKnowledgeBaseConnectionId(@Param("connectionId") String connectionId);

    List<KnowledgeBaseEntity> queryKnowledgeBaseListFromGlobalAndSelf(@Param("project_id") String projectId,
        @Param("workspaceId") String workspaceId, @Param("name") String name, @Param("fullName") String fullName,
        @Param("type") String type, @Param("status") String status, @Param("connectionId") String connectionId,
        @Param("createUserId") String createUserId, @Param("knowledgeBaseIds") List<String> knowledgeBaseIds,
        PageRowBounds pageRowBounds);

    /**
     * 检查是否存在指定domainId的数据
     *
     * @param domainId 租户ID
     * @return 存在返回true，不存在返回false
     */
    Boolean existsByDomainId(@Param("domain_id") String domainId);

    Integer countByDomainId(@Param("domain_id") String domainId);

    /**
     * 根据domainId查询所有知识库
     *
     * @param domainId 租户ID
     * @return 知识库列表
     */
    List<KnowledgeBaseEntity> selectByDomainId(@Param("domain_id") String domainId);

    List<KnowledgeBaseEntity> queryOtherSharedKnowledgeBaseList(@Param("project_id") String projectId,
        @Param("workspaceId") String workspaceId, @Param("name") String realQueryName,
        @Param("shareScopeEnums") List<ShareScopeEnum> shareScopeEnums, @Param("type") String type,
        @Param("status") String status, @Param("connectionId") String knowledgeBaseConnectionId,
        @Param("knowledgeBaseIds") List<String> knowledgeBaseIds, PageRowBounds pageRowBounds);

    List<KnowledgeBaseEntity> queryMySharedKnowledgeBaseList(@Param("project_id") String projectId,
        @Param("workspaceId") String workspaceId, @Param("name") String realQueryName,
        @Param("shareScopeEnums") List<ShareScopeEnum> shareScopeEnums, @Param("type") String type,
        @Param("status") String status, @Param("connectionId") String knowledgeBaseConnectionId,
        @Param("knowledgeBaseIds") List<String> knowledgeBaseIds, PageRowBounds pageRowBounds);

}
