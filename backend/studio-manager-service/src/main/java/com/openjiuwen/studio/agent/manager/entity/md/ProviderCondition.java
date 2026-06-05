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

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProviderCondition {
    private String query = null;

    private String authConfigStatus = null;

    private String id = null;

    private String projectId;

    private String workspaceId;

    private String excludeId;

    private List<String> providerIds = new ArrayList<>();

    public void sqlEscapeChar() {
        if (query != null) {
            query = AgentStringUtils.sqlEscapeChar(query);
        }

        if (id != null) {
            id = AgentStringUtils.sqlEscapeChar(id);
        }
    }
}
