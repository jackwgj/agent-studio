/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CSS文档信息实体类
 *
 * @since 2024-04-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListKnowledgeLabelInfo {
    private String id;

    private String name;

    private String color;

    @JsonProperty("create_time")
    private String createTime;
}
