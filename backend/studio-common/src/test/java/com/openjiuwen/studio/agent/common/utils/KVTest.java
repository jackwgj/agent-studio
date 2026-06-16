/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class KVTest {

    @Test
    void testOf() {
        KV kv = KV.of("key1", "value1");
        assertNotNull(kv);
        assertEquals("key1", kv.getKey());
        assertEquals("value1", kv.getValue());
    }

    @Test
    void testConstructor() {
        KV kv = new KV("k", 42);
        assertEquals("k", kv.getKey());
        assertEquals(42, kv.getValue());
    }

    @Test
    void testSettersAndGetters() {
        KV kv = new KV("a", "b");
        kv.setKey("newKey");
        kv.setValue("newValue");
        assertEquals("newKey", kv.getKey());
        assertEquals("newValue", kv.getValue());
    }

    @Test
    void testOf_NullKey() {
        KV kv = KV.of(null, "value");
        assertNull(kv.getKey());
        assertEquals("value", kv.getValue());
    }

    @Test
    void testOf_NullValue() {
        KV kv = KV.of("key", null);
        assertEquals("key", kv.getKey());
        assertNull(kv.getValue());
    }

    @Test
    void testEquals() {
        KV kv1 = KV.of("k", "v");
        KV kv2 = KV.of("k", "v");
        assertEquals(kv1, kv2);
    }

    @Test
    void testHashCode() {
        KV kv1 = KV.of("k", "v");
        KV kv2 = KV.of("k", "v");
        assertEquals(kv1.hashCode(), kv2.hashCode());
    }

    @Test
    void testToString() {
        KV kv = KV.of("key", "val");
        assertNotNull(kv.toString());
    }
}
