/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import lombok.Data;

@Data
public class WorkflowConfigMemoryVariable {

    @JsonProperty("aging_level")
    private String agingLevel = "session";

    private String description;

    private String name;

    private Boolean required = true;

    private String source = "pre_defined";

    @JsonProperty("storage_method")
    private String storageMethod = "assignment";

    private String type;

    private Object schema;

    private WorkflowFieldVOValue value;

}
