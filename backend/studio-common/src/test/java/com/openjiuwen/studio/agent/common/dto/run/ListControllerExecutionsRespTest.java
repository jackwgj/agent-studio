/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ListControllerExecutionsRespTest {

    @Test
    void testGettersAndSetters_AllFields() {
        ListControllerExecutionsResp resp = new ListControllerExecutionsResp()
                .setCount(5)
                .setExecutions(new ArrayList<>());

        assertEquals(5, resp.getCount());
        assertNotNull(resp.getExecutions());
    }

    @Test
    void testSetters_ReturnThis() {
        ListControllerExecutionsResp resp = new ListControllerExecutionsResp();
        assertSame(resp, resp.setCount(1));
        assertSame(resp, resp.setExecutions(new ArrayList<>()));
    }

    @Test
    void testEquals_SameObject() {
        ListControllerExecutionsResp resp = new ListControllerExecutionsResp().setCount(1);
        assertEquals(resp, resp);
    }

    @Test
    void testEquals_EqualObject() {
        ListControllerExecutionsResp r1 = new ListControllerExecutionsResp().setCount(1).setExecutions(null);
        ListControllerExecutionsResp r2 = new ListControllerExecutionsResp().setCount(1).setExecutions(null);
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        ListControllerExecutionsResp resp = new ListControllerExecutionsResp();
        assertNotEquals(null, resp);
    }

    @Test
    void testEquals_DifferentClass() {
        ListControllerExecutionsResp resp = new ListControllerExecutionsResp();
        assertNotEquals("string", resp);
    }

    @Test
    void testHashCode_Consistency() {
        ListControllerExecutionsResp r1 = new ListControllerExecutionsResp().setCount(1);
        ListControllerExecutionsResp r2 = new ListControllerExecutionsResp().setCount(1);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ListControllerExecutionsResp resp = new ListControllerExecutionsResp().setCount(1);
        assertNotNull(resp.toString());
    }
}
