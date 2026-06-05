/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity.md;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.openjiuwen.studio.agent.common.utils.AgentStringUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ModelServiceCondition {
    private String modelType = null;

    private String modelName = null;

    private Boolean functioncall = null;

    private String projectId;

    private String workspaceId;

    private String providerId;

    private String serviceName;

    private List<String> modelTypes;

    private String id;

    private String publishStatus;

    private Collection<String> idList;

    private String freeProviderId;

    public void sqlEscapeChar() {
        if (modelName != null) {
            modelName = AgentStringUtils.sqlEscapeChar(modelName);
        }
        if (serviceName != null) {
            serviceName = AgentStringUtils.sqlEscapeChar(serviceName);
        }
        if (id != null) {
            id = AgentStringUtils.sqlEscapeChar(id);
        }
    }
}
