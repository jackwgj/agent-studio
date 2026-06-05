/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.nl;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@Validated
public class WorkflowResource {
    @NotNull
    @JsonProperty("workflow_id")
    @Length(max = 200)
    private String workflowId;

    @NotNull
    @Length(max = 200)
    private String name;

    @Length(max = 200)
    private String code;

    @Length(max = 200)
    private String description;

    @Length(max = 200)
    private String type;

    @Length(max = 200)
    private String url;

    @Length(max = 200)
    @JsonProperty("last_version_id")
    private String lastVersionId;

    @Length(max = 200)
    @JsonProperty("resource_id")
    private String resourceId;
}
