/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.openjiuwen.studio.agent.runtime.mapper.handler.KnowledgeBaseConnectionParamListHandler;

import lombok.Data;

import java.util.List;

/**
 * 知识库连接实体
 *
 * @since 2026-03-22
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@TableName("t_knowledge_base_connection")
public class KnowledgeBaseConnectionEntity {

    @TableId
    private String id;

    private String connectorId;

    @TableField(value = "params", typeHandler = KnowledgeBaseConnectionParamListHandler.class)
    private List<KnowledgeBaseConnectionParam> params;
}