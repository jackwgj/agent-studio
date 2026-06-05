/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

public class WorkflowVersionEntity extends WorkflowEntity {
    /**
     * 发布版本唯一标识
     */
    @Getter
    @Setter
    @JsonProperty("version_id")
    private String versionId;

    /**
     * 发布版本名称
     */
    @JsonProperty("version_name")
    @Getter
    @Setter
    private String versionName;
}
