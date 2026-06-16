/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class RouterStrategyEntityTest {

    @Test
    void testNoArgsConstructor() {
        RouterStrategyEntity entity = new RouterStrategyEntity();
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getStrategyName());
    }

    @Test
    void testSettersAndGetters() {
        RouterStrategyEntity entity = new RouterStrategyEntity();
        entity.setId("id-1");
        entity.setStrategyName("name");
        entity.setStrategyType("type");
        entity.setStrategyKey("key");
        entity.setStrategyDescription("desc");
        entity.setServiceIdList("svc1,svc2");
        entity.setAuthIdList("auth1");
        entity.setStrategyTimeout(30);
        entity.setStrategyRetryCount(3);
        entity.setServiceCount(5);
        entity.setPrepareAttribute("{}");
        entity.setProjectId("proj-1");
        entity.setWorkspaceId("ws-1");
        entity.setDomainId("dom-1");
        entity.setCreatedByUserName("user1");
        entity.setLastUpdatedByUserName("user2");
        entity.setCreatedDate(1000L);
        entity.setLastUpdatedDate(2000L);
        entity.setTraceId("trace-1");

        assertEquals("id-1", entity.getId());
        assertEquals("name", entity.getStrategyName());
        assertEquals("type", entity.getStrategyType());
        assertEquals("key", entity.getStrategyKey());
        assertEquals("desc", entity.getStrategyDescription());
        assertEquals("svc1,svc2", entity.getServiceIdList());
        assertEquals("auth1", entity.getAuthIdList());
        assertEquals(30, entity.getStrategyTimeout());
        assertEquals(3, entity.getStrategyRetryCount());
        assertEquals(5, entity.getServiceCount());
        assertEquals("{}", entity.getPrepareAttribute());
        assertEquals("proj-1", entity.getProjectId());
        assertEquals("ws-1", entity.getWorkspaceId());
        assertEquals("dom-1", entity.getDomainId());
        assertEquals("user1", entity.getCreatedByUserName());
        assertEquals("user2", entity.getLastUpdatedByUserName());
        assertEquals(1000L, entity.getCreatedDate());
        assertEquals(2000L, entity.getLastUpdatedDate());
        assertEquals("trace-1", entity.getTraceId());
    }

    @Test
    void testChainAccessors() {
        RouterStrategyEntity entity = new RouterStrategyEntity()
                .setId("id-1")
                .setStrategyName("name")
                .setStrategyType("type");
        assertSame(entity, entity.setId("id-2"));
        assertEquals("id-2", entity.getId());
    }

    @Test
    void testDefaultValues() {
        RouterStrategyEntity entity = new RouterStrategyEntity();
        assertEquals(0L, entity.getCreatedDate());
        assertEquals(0L, entity.getLastUpdatedDate());
    }
}
