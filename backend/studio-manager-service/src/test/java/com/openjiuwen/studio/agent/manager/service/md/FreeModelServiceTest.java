/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class FreeModelServiceTest {
    private FreeModelService service;

    @BeforeEach
    public void beforeEach() {
        service = new FreeModelService();
        ReflectionTestUtils.setField(service, "modelCfgs",
            "free-deepseek-v3#llm_token_count_deepseek_v3,free-deepseek-r1#llm_token_count_deepseek_R1#invalid");
        service.postConstruct();
    }

    @Test
    public void freeModelCheckTest() {
        Assertions.assertTrue(service.isFreeModel("free-deepseek-v3"));
        Assertions.assertTrue(service.isFreeModel("free-deepseek-r1"));
        Assertions.assertFalse(service.isFreeModel("invalid"));
    }
}
