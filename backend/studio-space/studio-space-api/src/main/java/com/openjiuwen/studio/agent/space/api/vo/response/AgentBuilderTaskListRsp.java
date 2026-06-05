/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderTaskInfoVo;
import com.openjiuwen.studio.agent.space.api.vo.request.PageInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 任务列表查询返回类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AgentBuilderTaskListRsp extends PageInfo {
    @JsonProperty("task_list")
    private List<AgentBuilderTaskInfoVo> taskList;

    @JsonProperty("total")
    private long total;
}