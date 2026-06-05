/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.application;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderActionEntity;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 动作表操作类
 */
@Mapper
public interface AgentBuilderActionMapper extends BaseMapper<AgentBuilderActionEntity> {
    int insertBatchSomeColumn(List<AgentBuilderActionEntity> actionEntityList);
}