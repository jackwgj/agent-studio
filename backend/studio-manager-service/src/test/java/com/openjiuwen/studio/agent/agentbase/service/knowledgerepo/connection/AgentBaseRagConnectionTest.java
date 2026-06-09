/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class AgentBaseRagConnectionTest {

    @Test
    void isValid() {
        AgentBaseRagConnection
            agentBaseRagConnection = new AgentBaseRagConnection();
        agentBaseRagConnection.setEndpoint("http://127.0.0.1");
        Assertions.assertTrue(agentBaseRagConnection.isValid());
    }
}