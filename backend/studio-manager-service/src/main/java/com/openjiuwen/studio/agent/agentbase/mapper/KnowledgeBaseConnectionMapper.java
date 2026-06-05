/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnection;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeBaseConnectionMapper {

    int insert(@Param("connection") KnowledgeBaseConnectionEntity connection);

    KnowledgeBaseConnectionEntity find(@Param("id") String id, @Param("workspace_id") String workspaceId,
        @Param("project_id") String projectId);

    List<KnowledgeBaseConnectionEntity> listThirdPartyKnowledgeBases(@Param("name") String name,
        @Param("connector_id") String connectorId, @Param("status") String status,
        @Param("workspace_id") String workspaceId, @Param("project_id") String projectId);

    int delete(@Param("id") String id);

    int batchDelete(@Param("ids") List<String> ids);

    int update(@Param("connection") KnowledgeBaseConnectionEntity connection);

    String findByName(@Param("name") String name, @Param("id") String id, @Param("workspace_id") String workspaceId);

    List<KnowledgeBaseConnection> batchQueryKnowledgeBaseConnections(@Param("connectionIds") List<String> connectionIds,
        @Param("workspace_id") String workspaceId);

    Integer updateKnowledgeBaseConnectionStatus(@Param("connectionId") String connectionId,
        @Param("status") String status);

    Long countByDomainId(@Param("domain_id") String domainId);

    KnowledgeBaseConnectionEntity findById(@Param("connection_id") String connectionId);

    KnowledgeBaseConnectionEntity findByExternalRepoId(@Param("repo_id") String repoId);

    KnowledgeBaseConnectionEntity findConnectionBySegmentId(@Param("segmentId") String segmentId);

    List<KnowledgeBaseConnectionEntity> findDefaultConnection(@Param("connectorId") String connectorId);

    KnowledgeBaseConnectionEntity findConnectionById(@Param("connectionId") String connectionId);
}
