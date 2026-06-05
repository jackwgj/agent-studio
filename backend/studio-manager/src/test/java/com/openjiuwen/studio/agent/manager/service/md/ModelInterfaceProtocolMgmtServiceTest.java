/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ModelInterfaceProtocolMgmtServiceTest extends BaseTest {
    @Autowired
    private ModelInterfaceProtocolMgmtService service;

    @Autowired
    private ModelInterfaceProtocolService protocolService;

    @Test
    public void testProtocolService() throws InterruptedException {
        ReflectionTestUtils.setField(protocolService, "pocAgentBuilderEnable", true);
        ReflectionTestUtils.setField(protocolService, "delayTime", 1);
        protocolService.postConstruct();
        Thread.sleep(100);
    }
}
