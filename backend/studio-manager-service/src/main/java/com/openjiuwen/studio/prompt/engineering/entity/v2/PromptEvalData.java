/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromptEvalData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 测试数据唯一标识符
     */
    @JsonProperty("id")
    private String id;

    /**
     * 引用该数据的任务，多个任务ID以某种格式存储在文本中
     */
    @JsonProperty("task_id_list")
    private String taskIdList;

    /**
     * obs存储路径
     */
    @JsonProperty("obs_path")
    private String obsPath;

    /**
     * 项目id
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 空间id
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * 创建时间
     */
    @JsonProperty("created_time")
    private Date createdTime;

    /**
     * 工具创建者
     */
    @JsonProperty("creator")
    private String creator;

    /**
     * 工具创建者user id
     */
    @JsonProperty("creator_id")
    private String creatorId;

    /**
     * 更新时间
     */
    @JsonProperty("updated_time")
    private Date updatedTime;
}
