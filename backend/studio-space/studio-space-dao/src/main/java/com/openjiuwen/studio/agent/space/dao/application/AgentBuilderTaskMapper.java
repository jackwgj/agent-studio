/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderTaskEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 任务表操作类
 */
@Mapper
public interface AgentBuilderTaskMapper extends BaseMapper<AgentBuilderTaskEntity> {
    default AgentBuilderTaskEntity getTaskByIdAndTenantAndUser(String id, String domainId, String userId) {
        LambdaQueryWrapper<AgentBuilderTaskEntity> wrapper = new LambdaQueryWrapper<AgentBuilderTaskEntity>().eq(
                AgentBuilderTaskEntity::getId, id)
            .eq(AgentBuilderTaskEntity::getDomainId, domainId)
            .eq(AgentBuilderTaskEntity::getDeleted, false)
            .eq(AgentBuilderTaskEntity::getCreatedByUserId, userId);
        return selectOne(wrapper);
    }

    /**
     * 更新任务状态，只支持更新完成态之前的状态
     */
    @Update(
        "UPDATE ws_agent_builder_task_def SET status = #{status}, last_updated_date = #{lastUpdatedDate} WHERE id = #{id} AND status < 40")
    int updateStatusById(AgentBuilderTaskEntity taskEntity);

    /**
     * 删除任务
     */
    @Update(
        "UPDATE ws_agent_builder_task_def SET status = #{status}, deleted = true, last_updated_date = #{lastUpdatedDate} WHERE id = #{id}")
    int deletedById(AgentBuilderTaskEntity taskEntity);

    @Update("UPDATE ws_agent_builder_task_def SET name = #{name}, last_updated_date = #{lastUpdatedDate} WHERE id = #{id}")
    int updateTaskNameById(AgentBuilderTaskEntity taskEntity);

    @Update(
        "UPDATE ws_agent_builder_task_def SET name = #{name}, last_updated_date = #{lastUpdatedDate} WHERE id = #{id} AND name = '未命名任务'")
    int updateTaskNameByAgent(AgentBuilderTaskEntity taskEntity);

    /**
     * 批量更新任务状态和最后更新时间
     */
    @Update(
        "<script>" + "UPDATE ws_agent_builder_task_def " + "SET status = CASE " + "<foreach collection='list' item='item'>"
            + "  WHEN id = #{item.id} THEN #{item.status} " + "</foreach>" + "  ELSE status " + "END, "
            + "last_updated_date = CASE " + "<foreach collection='list' item='item'>"
            + "  WHEN id = #{item.id} THEN #{item.lastUpdatedDate} " + "</foreach>" + "  ELSE last_updated_date "
            + "END " + "WHERE id IN " + "<foreach collection='list' item='item' open='(' separator=',' close=')'>"
            + "#{item.id}" + "</foreach>" + "</script>")
    int batchUpdateStatus(@Param("list") List<AgentBuilderTaskEntity> taskEntities);
}
