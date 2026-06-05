/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 发布版本 数据库操作mapper
 *
 */
@Mapper
public interface ReleaseVersionMapper {
    /**
     * 插入一条发布版本记录到数据库
     *
     * @param releaseVersion releaseVersion
     * @return int
     */
    int insert(ReleaseVersion releaseVersion);

    /**
     * 根据id从数据库查询发布版本
     *
     * @param id id
     * @return version
     */
    ReleaseVersion selectByPrimaryKey(@Param("id") String id);

    /**
     * 根据versionId和appId从数据库查询发布版本
     *
     * @param versionId versionId
     * @return version
     */
    ReleaseVersion selectByAppIdAndVersionId(@Param("appId") String appId, @Param("versionId") String versionId);

    ReleaseVersion selectByAppIdAndVersionIdWithoutStatus(@Param("appId") String appId, @Param("versionId") String versionId);

    /**
     * 根据appId从数据库查询发布版本列表
     *
     * @param appId appId
     * @return version list
     */
    List<ReleaseVersion> selectByAppId(@Param("appId") String appId);

    /**
     * 根据appId从数据库查询发布版本列表
     *
     * @param appId appId
     * @param limit 最大数量限制
     * @return version list
     */
    List<ReleaseVersion> selectByAppIdWithLimit(@Param("appId") String appId, @Param("limit") int limit);

    /**
     * 根据versionId从数据库删除对应agent版本
     *
     * @param id id
     * @return int
     */
    int deleteByPrimaryKey(@Param("id") String id);

    /**
     * 根据id从数据库软删除对应ReleaseVersion
     *
     * @param id id
     * @return int
     */
    int softDeleteByPrimaryKey(@Param("id") String id);

    /**
     * 根据插件ID从数据库中删除对应的agent版本信息
     *
     * @param id 插件的唯一标识符
     * @param twoYearsAgo 两年前的时间，用于限定删除的版本范围
     * @return int
     */
    int deleteByPluginId(@Param("id") String id, @Param("twoYearsAgo") LocalDateTime twoYearsAgo);

    /**
     * 根据appId从数据库删除对应agent版本
     *
     * @param appId appId
     * @return int
     */
    int deleteByAppId(@Param("appId") String appId);

    /**
     * 根据appId从数据库软删除对应ReleaseVersion
     *
     * @param appId appId
     * @return int
     */
    int softDeleteByAppId(@Param("appId") String appId);

    List<String> findReleaseVersionToBeDeleted(@Param("softDeleteTtl") LocalDateTime softDeleteTtl);

    int deleteByPrimaryKeys(@Param("releaseVersionIds") List<String> releaseVersionIds);

    /**
     * 根据appId从数据库删除对应agent版本
     *
     * @param appId appId
     * @param versionId versionId
     * @return 删除数量
     */
    int deleteByAppIdAndVersionId(@Param("appId") String appId, @Param("versionId") String versionId);

    /**
     * 根据appId从数据库删除对应agent版本
     *
     * @param releaseVersions releaseVersion列表
     * @return Map
     */
    List<ReleaseVersion> queryByWfVersions(@Param("releaseVersions") List<ReleaseVersion> releaseVersions);

    /**
     * 根据appId获取app最新版本列表
     *
     * @param appIds 应用id列表
     * @return 返回最新版本列表
     */
    List<ReleaseVersion> queryLastVersionByAppIds(@Param("appIds") Set<String> appIds);

    /**
     * 恢复版本记录
     * @param appId appId
     * @param versionId 版本ID
     */
    void restoreReleaseVersion(@Param("appId") String appId, @Param("versionId") String versionId);

    void updateDsl(ReleaseVersion existVersion);
}
