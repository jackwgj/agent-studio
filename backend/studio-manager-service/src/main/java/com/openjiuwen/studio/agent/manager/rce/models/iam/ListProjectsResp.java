/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.iam;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * ListProjectsResp
 *
 */
@Data
public class ListProjectsResp {
    @JsonProperty("projects")
    private List<Project> projects;
}
