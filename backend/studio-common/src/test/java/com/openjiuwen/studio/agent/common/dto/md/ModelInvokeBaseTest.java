/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ModelInvokeBaseTest {

    @Test
    void testSetGetModel() {
        ModelInvokeBase base = new ModelInvokeBase();
        assertNull(base.getModel());
        base.setModel("gpt-4");
        assertEquals("gpt-4", base.getModel());
    }

    @Test
    void testSetGetRefresh() {
        ModelInvokeBase base = new ModelInvokeBase();
        assertNull(base.getRefresh());
        base.setRefresh("true");
        assertEquals("true", base.getRefresh());
    }

    @Test
    void testDefaultConstructor() {
        ModelInvokeBase base = new ModelInvokeBase();
        assertNotNull(base);
        assertNull(base.getModel());
        assertNull(base.getRefresh());
    }
}
