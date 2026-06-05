/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeSourceConnection;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
public class AgentBaseRagConnection extends KnowledgeSourceConnection {

    public boolean isValid() {
        Objects.requireNonNull(super.getEndpoint(), "endpoint should not be null");
        return true;
    }
}
