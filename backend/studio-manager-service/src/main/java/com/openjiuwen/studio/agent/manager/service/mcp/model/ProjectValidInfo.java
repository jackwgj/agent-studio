/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description 模型参数
 * @since 2025/7/3
 **/
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
public class ProjectValidInfo {

    private String id;

    @JsonProperty("element_id")
    private String elementId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("project_name")
    private String projectName;
}
