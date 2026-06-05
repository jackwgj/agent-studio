/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.StructuredMessageEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructuredMessageMapper {

    /**
     * 创建StructuredMessageEntity
     *
     * @param structuredMessageEntity 创建结构化信息
     * @return int
     */
    int createStructuredMessage(@Param("structuredMessageEntity") StructuredMessageEntity structuredMessageEntity);

    /**
     * 根据主键批量删除
     *
     * @param ids ids
     * @param projectId projectId
     * @return int
     */
    int deleteByIds(@Param("ids") List<String> ids, @Param("projectId") String projectId);

    /**
     * 查询StructuredMessageEntity
     *
     * @param structuredMessageEntity 筛选信息
     * @return StructuredMessageEntity
     */
    List<StructuredMessageEntity> getStructuredMessageEntity(
            @Param("structuredMessageEntity") StructuredMessageEntity structuredMessageEntity,
            @Param("order") String order);


    /**
     * 更新StructuredMessageEntity
     *
     * @param structuredMessageEntity
     * @return 影响的行数
     */
    int updateByPrimaryKey(@Param("structuredMessageEntity") StructuredMessageEntity structuredMessageEntity);

    /**
     * 按id查询StructuredMessageEntity
     *
     * @param ids 消息模板id列表
     * @return StructuredMessageEntity
     */
    List<StructuredMessageEntity> getByIds(@Param("ids") List<String> ids, @Param("workspaceId") String workspaceId,
        @Param("domainId") String domainId, @Param("userId") String userId);

}
