/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.ApiKeysEntity;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApiKeysMapper {

    void createApiKey(@Param("apiKeys") ApiKeysEntity apiKeys);

    List<ApiKeysEntity> selectList(
        @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId,
        @Param("pageSize") int pageSize,
        @Param("offset") int offset,
        @Param("userId") String userId
    );
    ApiKeysEntity selectById(@Param("id") String id);

    void deleteById(
        @Param("id") String id);

    int count(
        @Param("apikeyName") String apikeyName,
        @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId,
        @Param("userId") String userId
    );
}
