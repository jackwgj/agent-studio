/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConversationDeleteRespTest {

    @Test
    void testGettersAndSetters_AllFields() {
        ConversationDeleteResp resp = new ConversationDeleteResp()
                .setId("conv-123");

        assertEquals("conv-123", resp.getId());
    }

    @Test
    void testSetters_ReturnThis() {
        ConversationDeleteResp resp = new ConversationDeleteResp();
        assertSame(resp, resp.setId("id"));
    }

    @Test
    void testEquals_SameObject() {
        ConversationDeleteResp resp = new ConversationDeleteResp().setId("id");
        assertEquals(resp, resp);
    }

    @Test
    void testEquals_EqualObject() {
        ConversationDeleteResp r1 = new ConversationDeleteResp().setId("id");
        ConversationDeleteResp r2 = new ConversationDeleteResp().setId("id");
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        ConversationDeleteResp resp = new ConversationDeleteResp();
        assertNotEquals(null, resp);
    }

    @Test
    void testEquals_DifferentClass() {
        ConversationDeleteResp resp = new ConversationDeleteResp();
        assertNotEquals("string", resp);
    }

    @Test
    void testHashCode_Consistency() {
        ConversationDeleteResp r1 = new ConversationDeleteResp().setId("id");
        ConversationDeleteResp r2 = new ConversationDeleteResp().setId("id");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ConversationDeleteResp resp = new ConversationDeleteResp().setId("id");
        assertNotNull(resp.toString());
    }
}
