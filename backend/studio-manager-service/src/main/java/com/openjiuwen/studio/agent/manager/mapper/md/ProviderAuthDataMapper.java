/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.md;

import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProviderAuthDataMapper {
    int insert(ProviderAuthData config);

    /**
     * 查询供应商认证配置
     *
     * @param projectId 项目ID
     * @param workspaceId 空间ID
     * @param providerId 供应商ID
     * @return 认证配置
     */
    List<ProviderAuthMetadata> selectProviderAuthData(@Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId, @Param("providerId") String providerId);

    ProviderAuthData selectByMetaId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId,
        @Param("metaId") String metaId);

    ProviderAuthData selectByModelId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId,
        @Param("modelId") String modelId);

    void deleteByProviderId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId,
        @Param("providerId") String providerId);

    void clearByProviderId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId,
        @Param("providerId") String providerId);

    List<ProviderAuthData> selectByProviderId(@Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId, @Param("providerId") String providerId);

    int updateAuthData(@Param("id") String id, @Param("authType") String authType, @Param("authInfo") String authInfo,
        @Param("updatedDate") long updatedDate);
    ProviderAuthData  queryByProviderId(@Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId, @Param("providerId") String providerId);
    /**
     * 查询供应商认证配置
     *
     * @param projectId 项目ID
     * @param workspaceId 空间ID
     * @param providerId 供应商ID
     * @param authMetadataId 认证配置ID
     * @return 认证配置
     */
    List<ProviderAuthMetadata> selectAuthDataByProviderOrMetaId(@Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId, @Param("providerId") String providerId,
        @Param("authMetadataId") String authMetadataId);

    ProviderAuthData selectById(@Param("id") String id);

    ProviderAuthData selectByIdAndProjectIdAndWorkSpaceId(@Param("id") String id, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    List<ProviderAuthData> selectByIds(@Param("ids") List<String> ids);

    List<ProviderAuthData> selectByProviderIds(
        @Param("ids") Collection<String> ids);

    void deleteById(@Param("id") String id);

    void clearById(@Param("id") String id);

    List<ProviderAuthData> selectAllAuthByTime(@Param("time") long time);

    List<ProviderAuthData> queryNotSyncDatas();

    void updateSyncStatus(@Param("id") String id);

    List<ProviderAuthData> selectByProviders(@Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId, @Param("providers") Collection<String> providers);

    /**
     * 数据备份
     */
    int insertBackup(ProviderAuthData config);

    List<ProviderAuthData> queryBackupByUpdateTime(@Param("lastUpdatedDate") Long lastUpdatedDate);

    void deleteBackupByIds(@Param("ids") List<String> ids);
}
