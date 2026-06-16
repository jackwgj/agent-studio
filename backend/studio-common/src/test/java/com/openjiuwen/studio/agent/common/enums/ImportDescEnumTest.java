/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImportDescEnumTest {

    @Test
    void testValues() {
        assertEquals(7, ImportDescEnum.values().length);
    }

    @Test
    void testValueOf() {
        assertEquals(ImportDescEnum.NEW_RESOURCE, ImportDescEnum.valueOf("NEW_RESOURCE"));
        assertEquals(ImportDescEnum.UPDATE_RESOURCE, ImportDescEnum.valueOf("UPDATE_RESOURCE"));
        assertEquals(ImportDescEnum.ERROR, ImportDescEnum.valueOf("ERROR"));
    }

    @Test
    void testGetCode() {
        assertEquals("newResource", ImportDescEnum.NEW_RESOURCE.getCode());
        assertEquals("updateResource", ImportDescEnum.UPDATE_RESOURCE.getCode());
        assertEquals("newResourceVersion", ImportDescEnum.NEW_RESOURCE_VERSION.getCode());
        assertEquals("resourceExists", ImportDescEnum.RESOURCE_EXISTS.getCode());
        assertEquals("resourceMatch", ImportDescEnum.RESOURCE_MATCH.getCode());
        assertEquals("resourceNotMatch", ImportDescEnum.RESOURCE_NOT_MATCH.getCode());
        assertEquals("error", ImportDescEnum.ERROR.getCode());
    }

    @Test
    void testGetDesc() {
        assertNotNull(ImportDescEnum.NEW_RESOURCE.getDesc());
        assertNotNull(ImportDescEnum.ERROR.getDesc());
    }
}
