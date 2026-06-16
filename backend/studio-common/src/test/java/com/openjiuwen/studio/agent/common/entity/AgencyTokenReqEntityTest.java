/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AgencyTokenReqEntityTest {

    @Test
    void testGettersAndSetters() {
        AgencyTokenReqEntity entity = new AgencyTokenReqEntity();
        AuthEntity auth = new AuthEntity();
        entity.setAuth(auth);
        assertEquals(auth, entity.getAuth());
    }

    @Test
    void testDefaultAuth() {
        AgencyTokenReqEntity entity = new AgencyTokenReqEntity();
        assertNull(entity.getAuth());
    }

    @Test
    void testEqualsAndHashCode() {
        AgencyTokenReqEntity entity1 = new AgencyTokenReqEntity();
        AgencyTokenReqEntity entity2 = new AgencyTokenReqEntity();
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testToString() {
        AgencyTokenReqEntity entity = new AgencyTokenReqEntity();
        assertNotNull(entity.toString());
    }
}
