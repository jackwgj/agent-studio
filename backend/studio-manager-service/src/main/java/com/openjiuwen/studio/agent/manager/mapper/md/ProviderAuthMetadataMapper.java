/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.md;

import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProviderAuthMetadataMapper {
    ProviderAuthMetadata selectById(@Param("id") String id);

    List<ProviderAuthMetadata> selectByProvider(@Param("provider") String provider);

    int insert(ProviderAuthMetadata record);

    void deleteById(@Param("id") String id);

    void deleteByProviderId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId,
        @Param("providerId") String providerId);

    void updateMetadata(@Param("id") String id, @Param("authType") String authType, @Param("authInfo") String authInfo,
        @Param("updatedDate") long updatedDate);

    List<ProviderAuthMetadata> selectByProjectWorkspaceProvider(@Param("projectId") String projectId,
                                                                @Param("workspaceId") String workspaceId,
                                                                @Param("providerId") String providerId);

    // 数据删除备份
    int insertBackup(ProviderAuthMetadata record);

    List<ProviderAuthMetadata> queryBackupByUpdateTime(@Param("lastUpdatedDate") Long lastUpdatedDate);

    void deleteBackupByIds(@Param("ids") List<String> ids);

    List<ProviderAuthMetadata> selectByIds(@Param("ids") Collection<String> ids);
}
