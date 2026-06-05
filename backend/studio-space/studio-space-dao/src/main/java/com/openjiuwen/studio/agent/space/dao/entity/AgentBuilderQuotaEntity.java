/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.springframework.data.annotation.Id;

/**
 * 配额表
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@TableName(value = "ws_agent_builder_quota_def", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class AgentBuilderQuotaEntity extends BaseProperties {
    /**
     * 配额id
     */
    @Id
    @TableField("id")
    private String id;

    @TableField("type")
    private String type;

    @TableField("quota")
    private int quota;

    @TableField("used")
    private int used;
}