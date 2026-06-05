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

import org.apache.commons.lang.StringUtils;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RouterStrategyEntityCondition {
    private String projectId;

    private String workspaceId;

    private String query;

    private int limit;

    private int offset;

    public void sqlEscapeChar() {
        query = StringUtils.isBlank(query) ? null : AgentStringUtils.sqlEscapeChar(query);
    }
}
