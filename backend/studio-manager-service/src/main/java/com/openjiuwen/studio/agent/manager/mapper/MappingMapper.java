/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.dto.ResourceMapping;
import com.openjiuwen.studio.agent.manager.entity.MappingEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 关联表 mapper
 *
 */
@Mapper
public interface MappingMapper {
    /**
     * 插入单条记录
     *
     * @param mappingEntity mappingEntity
     * @return int
     */
    int insert(MappingEntity mappingEntity);

    /**
     * 批量插入记录
     *
     * @param records records
     * @return int
     */
    int insertBatch(@Param("records") List<MappingEntity> records);

    /**
     * 根据资源ID列表批量删除记录
     *
     * @param appId appId
     * @param resourceIds resourceIds
     * @return int
     */
    int deleteBatch(@Param("appId") String appId, @Param("resourceIds") List<String> resourceIds);

    /**
     * 根据应用APPID及appVersion删除关联记录
     * 如果appVersion为null，isAllVersion=true则删除所有版本依赖的资源
     * 否则只删除无appVersion的资源依赖即删除app草稿的资源依赖
     *
     * @param appId 应用id
     * @param appVersion 应用版本
     * @param isAllVersion 是否删除所有版本
     * @return
     */
    int deleteBatchByAppId(@Param("appId") String appId, @Param("appVersion") String appVersion,
        @Param("isAllVersion") boolean isAllVersion);

    /**
     * 根据应用APPID及appVersion软删除关联记录
     * 如果appVersion为null，isAllVersion=true则删除所有版本依赖的资源
     * 否则只软删除无appVersion的资源依赖即删除app草稿的资源依赖
     *
     * @param appId 应用id
     * @param appVersion 应用版本
     * @param isAllVersion 是否删除所有版本
     * @return
     */
    int softDeleteBatchByAppId(@Param("appId") String appId, @Param("appVersion") String appVersion,
                           @Param("isAllVersion") boolean isAllVersion);

    /**
     * 根据应用APPID及关联资源类型删除关联记录
     *
     * @param appId 应用id
     * @param resourceType 关联资源类型
     * @return 删除记录数量
     */
    int deleteBatchByAppIdAndResourceType(@Param("appId") String appId, @Param("resourceType") String resourceType);

    /**
     * 根据应用ID和关联资源类型查询关联信息
     *
     * @param appId appId
     * @return List
     */
    List<MappingEntity> selectByAppIdAndResourceType(@Param("appId") String appId,
        @Param("resourceType") String resourceType);

    List<MappingEntity> selectAllByAppId(@Param("appId") String appId);

    /**
     * 根据应用ID和资源ID查询关联信息
     *
     * @param appId appId
     * @param resourceId resourceId
     * @return List
     */
    MappingEntity selectByAppIdAndResourceId(@Param("appId") String appId, @Param("resourceId") String resourceId);

    /**
     * 根据appId，appVersion,resourceType查询关联资源
     *
     * @param appId 应用id
     * @param appVersion 应用版本，如果传null，表示查询草稿
     * @param resourceType 资源类型
     * @param valid 是否生效
     * @return 资源列表
     */
    List<MappingEntity> selectByAppIdAndAppVersion(@Param("appId") String appId, @Param("appVersion") String appVersion,
        @Param("resourceType") String resourceType, @Param("valid") Boolean valid);

    /**
     * 根据MappingEntity列表查询关联资源
     * @param queryList MappingEntity列表
     * @return 资源列表
     */
    List<MappingEntity> selectByMappingList(@Param("queryList") List<MappingEntity> queryList);

    /**
     * 根据引用关系嵌套查询引用中的引用
     *
     * @param refs 引用关系，从中取resourceId和resourceVersion进行查询
     * @param resourceType 资源类型
     * @return 资源列表
     */
    List<MappingEntity> selectByRefs(@Param("refs") List<MappingEntity> refs,
        @Param("resourceType") String resourceType);

    /**
     * 根据resourceId查询关联信息
     *
     * @param resourceId resourceId
     * @return List
     */
    List<MappingEntity> selectByResourceId(@Param("resourceId") String resourceId);

    List<MappingEntity> selectByResourceIdAndVersionId(@Param("resourceId") String resourceId,
        @Param("resourceVersion") String resourceVersion,@Param("workspaceId") String workspaceId,
        @Param("appType") String appType, @Param("referenceType") String referenceType);

    /**
     * 更新指定资源的valid为false
     *
     * @param resourceId 资源ID
     * @param resourceVersion 资源版本，注意！如果传null，会将所有版本和草稿的资源绑定关系全部置为false！
     * @return 更新数量
     */
    int updateValidByResourceIdAndVersionId(@Param("resourceId") String resourceId,
        @Param("resourceVersion") String resourceVersion);

    int updateByPrimaryKeySelective(MappingEntity mappingEntity);

    int updateAppNameByAppId(@Param("appId") String appId, @Param("appName") String appName);

    List<ResourceMapping> getByIds(@Param("ids") List<String> ids);

    /**
     * 根据应用APPID及appVersion删除关联类型为type的记录
     *
     * @param appId 应用id
     * @param appVersion 应用版本
     * @param type 类型
     * @return int
     */
    int deleteBatchByAppIdType(@Param("appId") String appId, @Param("appVersion") String appVersion,
        @Param("type") String type);

    int updateValidByResourceId(@Param("resourceId") String resourceId);

    /**
     * 更新资源输入参数
     * @param mappingEntity
     * @return
     */
    int updateResourceParameterByAppIDAndResourceId(MappingEntity mappingEntity);

    void updateShareRelationValidStatusByShareResourceId(@Param("resourceId") String resourceId, @Param("valid") boolean valid);

    int countMappingEntityListByAppIdAndResourceId(@Param("appId") String appId, @Param("appVersion") String appVersion,
        @Param("resourceId") String resourceId, @Param("resourceVersion") String resourceVersion);

    /**
     * 批量更新mcpServer可使用工具记录
     *
     * @param records records
     * @return int
     */
    int updateMcpServerBatch(@Param("records") List<MappingEntity> records);

    int updateById(MappingEntity mappingEntity);
}
