/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.dto.ToolCredential;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 功能描述
 *
 */
@Mapper
public interface ToolCredentialMapper {
    /**
     * 创建toolCredential
     *
     * @param credential
     * @return 凭证
     */
    int insert(ToolCredential credential);

    /**
     * 查找toolCredential
     *
     * @param projectId
     * @param userId
     * @param toolId
     * @return 凭证credential
     */
    ToolCredential selectByToolId(@Param("projectId") String projectId, @Param("toolId") String toolId, @Param("workspaceId") String workspaceId);

    ToolCredential selectByToolIdAndDomainId(@Param("toolId") String toolId, @Param("domainId") String domainId, @Param("workspaceId") String workspaceId);

    int deleteByToolId(@Param("projectId") String projectId, @Param("userId") String userId,
        @Param("toolId") String toolId, @Param("workspaceId") String workspaceId);

    int updateByToolId(@Param("projectId") String projectId, @Param("userId") String userId,
        @Param("toolId") String toolId, @Param("authKeys") String authKeys, @Param("workspaceId") String workspaceId);

    /**
     * 查找toolCredential的auth_keys字段
     *
     * @param toolId 工具ID
     * @return auth_keys字段值列表
     */
    List<String> selectAuthKeysByToolId(@Param("toolId") String toolId);

    List<ToolCredential> selectUserIdByDomainId(@Param("domainId") String domainId);

    void deleteByDomainId(String domainId);
}
