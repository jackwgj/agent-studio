/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AnalyticsEventReqTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        AnalyticsEventReq req = new AnalyticsEventReq()
                .setEventType("click")
                .setAppType("agent")
                .setChannel("web")
                .setEventId("evt-001");
        assertEquals("click", req.getEventType());
        assertEquals("agent", req.getAppType());
        assertEquals("web", req.getChannel());
        assertEquals("evt-001", req.getEventId());
    }

    @Test
    void testEquals_SameInstance() {
        AnalyticsEventReq req = new AnalyticsEventReq().setEventType("click");
        assertEquals(req, req);
    }

    @Test
    void testEquals_EqualObjects() {
        AnalyticsEventReq r1 = new AnalyticsEventReq().setEventType("click").setAppType("agent");
        AnalyticsEventReq r2 = new AnalyticsEventReq().setEventType("click").setAppType("agent");
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        AnalyticsEventReq req = new AnalyticsEventReq();
        assertNotEquals(null, req);
    }

    @Test
    void testEquals_DifferentClass() {
        AnalyticsEventReq req = new AnalyticsEventReq();
        assertNotEquals("string", req);
    }

    @Test
    void testHashCode_Consistency() {
        AnalyticsEventReq r1 = new AnalyticsEventReq().setEventType("click").setAppType("agent");
        AnalyticsEventReq r2 = new AnalyticsEventReq().setEventType("click").setAppType("agent");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AnalyticsEventReq req = new AnalyticsEventReq().setEventType("click");
        assertNotNull(req.toString());
    }
}
