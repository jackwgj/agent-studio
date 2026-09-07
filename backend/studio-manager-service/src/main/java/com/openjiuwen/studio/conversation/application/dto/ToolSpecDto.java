/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Python describe 端点返回的内置工具描述（字段名与 t_tool 列对齐）。
 *
 * <p>{@code source} 为 transient 字段，仅描述工具来源，不写入 t_tool。</p>
 */
@Data
public class ToolSpecDto {

    @JsonProperty("tool_id")
    private String toolId;

    @JsonProperty("tool_display_name")
    private String toolDisplayName;

    @JsonProperty("tool_chinese_name")
    private String toolChineseName;

    @JsonProperty("tool_desc")
    private String toolDesc;

    @JsonProperty("type")
    private String type;

    @JsonProperty("intf_type")
    private String intfType;

    @JsonProperty("input_schema")
    private String inputSchema;

    @JsonProperty("output_schema")
    private String outputSchema;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("visibility")
    private String visibility;

    @JsonProperty("published")
    private Integer published;

    @JsonProperty("auth_required")
    private Boolean authRequired;

    @JsonProperty("is_input_list")
    private Boolean isInputList;

    @JsonProperty("is_output_list")
    private Boolean isOutputList;

    @JsonProperty("creator")
    private String creator;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("category")
    private String category;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("workspace_id")
    private String workspaceId;

    /** transient：仅描述工具来源（supervisor_builtin/generic_builtin/...），不落库。 */
    @JsonProperty("source")
    private String source;
}
