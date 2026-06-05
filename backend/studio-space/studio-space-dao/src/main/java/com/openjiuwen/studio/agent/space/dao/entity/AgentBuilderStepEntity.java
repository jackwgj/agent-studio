/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.springframework.data.annotation.Id;

/**
 * 步骤表
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@TableName(value = "ws_agent_builder_step_def", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class AgentBuilderStepEntity extends BaseProperties {
    /**
     * 步骤id
     */
    @Id
    @TableField("id")
    private String id;

    /**
     * agent名称
     */
    @JsonProperty("agent_name")
    private String agentName;

    /**
     * 关联的消息id
     */
    @TableField("message_id")
    private String messageId;

    /**
     * 是否已结束
     */
    @TableField("finished")
    private boolean finished;
}