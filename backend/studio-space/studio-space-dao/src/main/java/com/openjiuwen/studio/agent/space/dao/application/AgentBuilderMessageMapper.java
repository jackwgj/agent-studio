/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderMessageEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息表处理类
 */
@Mapper
public interface AgentBuilderMessageMapper extends BaseMapper<AgentBuilderMessageEntity> {
    int insertBatchSomeColumn(List<AgentBuilderMessageEntity> messageList);

    default List<AgentBuilderMessageEntity> getSystemMsgNewestByTaskId(@NotBlank @NotNull String taskId) {
        return selectList(new LambdaQueryWrapper<AgentBuilderMessageEntity>().eq(AgentBuilderMessageEntity::getTaskId, taskId)
            .eq(AgentBuilderMessageEntity::getType, 2)
            .eq(AgentBuilderMessageEntity::getDeleted, false)
            .orderBy(true, false, AgentBuilderMessageEntity::getCreatedDate));
    }

    default AgentBuilderMessageEntity getSystemMsgByIdAndTaskId(@NotBlank String id, @NotBlank @NotNull String taskId) {
        return selectOne(new LambdaQueryWrapper<AgentBuilderMessageEntity>().eq(AgentBuilderMessageEntity::getId, id)
            .eq(AgentBuilderMessageEntity::getTaskId, taskId)
            .eq(AgentBuilderMessageEntity::getDeleted, false));
    }

    default List<AgentBuilderMessageEntity> getAnswerMsgByQueryIdAndTaskId(@NotBlank String id,
        @NotBlank @NotNull String taskId) {
        return selectList(new LambdaQueryWrapper<AgentBuilderMessageEntity>().eq(AgentBuilderMessageEntity::getParentId, id)
            .eq(AgentBuilderMessageEntity::getType, 2)
            .eq(AgentBuilderMessageEntity::getTaskId, taskId)
            .eq(AgentBuilderMessageEntity::getDeleted, false));
    }

    @Select("SELECT * FROM ws_agent_builder_message_def q WHERE q.created_date < ( SELECT a.created_date FROM"
        + " ws_agent_builder_message_def a WHERE a.id = #{answerId} AND a.type = 2 AND a.task_id = #{taskId})"
        + " ORDER BY q.created_date DESC LIMIT 1;")
    AgentBuilderMessageEntity getBeforeMsgByAnswerId(String answerId, String taskId);
}
