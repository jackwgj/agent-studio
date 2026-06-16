/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AnalyticsEventRespTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        AnalyticsEventResp resp = new AnalyticsEventResp()
                .setErrorMessage("some error")
                .setErrorCode("ERR001")
                .setEventId("evt-001");
        assertEquals("some error", resp.getErrorMessage());
        assertEquals("ERR001", resp.getErrorCode());
        assertEquals("evt-001", resp.getEventId());
    }

    @Test
    void testEquals_SameInstance() {
        AnalyticsEventResp resp = new AnalyticsEventResp().setEventId("evt");
        assertEquals(resp, resp);
    }

    @Test
    void testEquals_EqualObjects() {
        AnalyticsEventResp r1 = new AnalyticsEventResp().setErrorCode("E1").setEventId("evt");
        AnalyticsEventResp r2 = new AnalyticsEventResp().setErrorCode("E1").setEventId("evt");
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        AnalyticsEventResp resp = new AnalyticsEventResp();
        assertNotEquals(null, resp);
    }

    @Test
    void testEquals_DifferentClass() {
        AnalyticsEventResp resp = new AnalyticsEventResp();
        assertNotEquals("string", resp);
    }

    @Test
    void testHashCode_Consistency() {
        AnalyticsEventResp r1 = new AnalyticsEventResp().setErrorCode("E1").setEventId("evt");
        AnalyticsEventResp r2 = new AnalyticsEventResp().setErrorCode("E1").setEventId("evt");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AnalyticsEventResp resp = new AnalyticsEventResp().setEventId("evt");
        assertNotNull(resp.toString());
    }
}
