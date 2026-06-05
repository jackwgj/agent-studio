/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import org.springframework.validation.annotation.Validated;

/**
 * 承载MCP Server的基本信息
 */
@Data
@Accessors(chain = true)
@Validated
public class McpServerBaseInfoVo {
    private String id;

    /**
     * ICON 图标
     */
    private String icon;

    /**
     * name 中文名
     */
    private String name;

    /**
     * name 英文名
     */
    @JsonProperty("name_en")
    private String nameEn;

    /**
     * 描述、简介-中文
     */
    private String description;

    /**
     * 描述、简介-英文
     */
    @JsonProperty("description_en")
    private String descriptionEn;

    /**
     * 内置 OR 个人
     */
    private String type;

    /**
     * MCP安装类型  NPX/UVX/SSE
     */
    @JsonProperty("deploy_type")
    private String deployType;

    /**
     * 查看次数
     */
    @JsonProperty("view_times")
    private long viewTimes;

    /**
     * 安装次数
     */
    @JsonProperty("install_times")
    private long installTimes;

    /**
     * 评分
     */
    @JsonProperty("score")
    private double score;
}
