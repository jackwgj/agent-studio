/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 导出依赖包请求
 *
 */
@Data
@Accessors(chain = true)
public class ProjectValidInfoResp {
    /**
     * 导出的所有mcp 主键列表
     */
    @JsonProperty("data")
    public List<ProjectValidInfo> projectValidInfos;
}
