/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.resource.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportResp {

    @JsonProperty("exportInfos")
    private List<ExportInfo> exportInfos;

    @JsonProperty("exportResults")
    private List<com.openjiuwen.studio.agent.manager.workflow.resource.model.ExportResult> exportResults;
}
