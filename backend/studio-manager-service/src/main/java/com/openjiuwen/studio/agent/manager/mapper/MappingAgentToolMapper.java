
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.dao.MappingAgentTool;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 功能描述 agent插件关联 mapper
 *
 */
@Mapper
public interface MappingAgentToolMapper {
    /**
     * 删除单条记录
     *
     * @param agentId agentId
     * @param toolId toolId
     * @param projectId projectId
     * @return int
     */
    int deleteByPrimaryKey(@Param("agentId") String agentId, @Param("toolId") String toolId,
        @Param("projectId") String projectId);

    /**
     * 根据toolId列表批量删除记录
     *
     * @param agentId agentId
     * @param toolIds toolIds
     * @param projectId projectId
     * @return int
     */
    int deleteBatch(@Param("agentId") String agentId, @Param("toolIds") List<String> toolIds,
        @Param("projectId") String projectId);

    /**
     * 根据agent id列表批量删除记录
     *
     * @param toolId toolId
     * @param agentIds agentIds
     * @param projectId projectId
     * @return int
     */
    int deleteBatchByAgentIds(@Param("toolId") String toolId, @Param("agentIds") List<String> agentIds,
        @Param("projectId") String projectId);

    /**
     * 插入单条记录
     *
     * @param record record
     * @return int
     */
    int insert(MappingAgentTool record);

    /**
     * 按需插入记录
     *
     * @param record record
     * @return int
     */
    int insertSelective(MappingAgentTool record);

    /**
     * 批量插入记录
     *
     * @param records records
     * @return int
     */
    int insertBatch(Set<MappingAgentTool> records);

    /**
     * 查询单条记录
     *
     * @param agentId agentId
     * @param toolId toolId
     * @param projectId projectId
     * @return MappingAgentTool
     */
    MappingAgentTool selectByPrimaryKey(@Param("agentId") String agentId, @Param("toolId") String toolId,
        @Param("projectId") String projectId);

    /**
     * 根据tool id列表批量查询记录
     *
     * @param agentId agentId
     * @param toolIds toolIds
     * @param projectId projectId
     * @return List
     */
    List<MappingAgentTool> selectBatchWithToolsOptional(@Param("agentId") String agentId,
        @Param("toolIds") List<String> toolIds, @Param("projectId") String projectId);

    /**
     * 查询除知识库插件外的其他插件依赖记录
     *
     * @param agentId agentId
     * @param projectId projectId
     * @return List
     */
    List<MappingAgentTool> selectBatchExcludeKnowledgeTool(@Param("agentId") String agentId,
        @Param("projectId") String projectId);

    /**
     * 按需更新记录字段
     *
     * @param record record
     * @return int
     */
    int updateByPrimaryKeySelective(MappingAgentTool record);
}
