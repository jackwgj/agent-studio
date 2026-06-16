/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchDeleteUserVariableMemoryResponseBodyTest {

    @Test
    void testGettersAndSetters_AllFields() {
        List<String> keys = Arrays.asList("key1", "key2");
        BatchDeleteUserVariableMemoryResponseBody body = new BatchDeleteUserVariableMemoryResponseBody()
                .setVariableKeys(keys);

        assertEquals(keys, body.getVariableKeys());
    }

    @Test
    void testSetters_ReturnThis() {
        BatchDeleteUserVariableMemoryResponseBody body = new BatchDeleteUserVariableMemoryResponseBody();
        assertSame(body, body.setVariableKeys(Arrays.asList("k")));
    }

    @Test
    void testEquals_SameObject() {
        BatchDeleteUserVariableMemoryResponseBody body = new BatchDeleteUserVariableMemoryResponseBody()
                .setVariableKeys(Arrays.asList("k"));
        assertEquals(body, body);
    }

    @Test
    void testEquals_EqualObject() {
        BatchDeleteUserVariableMemoryResponseBody b1 = new BatchDeleteUserVariableMemoryResponseBody()
                .setVariableKeys(Arrays.asList("k1", "k2"));
        BatchDeleteUserVariableMemoryResponseBody b2 = new BatchDeleteUserVariableMemoryResponseBody()
                .setVariableKeys(Arrays.asList("k1", "k2"));
        assertEquals(b1, b2);
    }

    @Test
    void testEquals_Null() {
        BatchDeleteUserVariableMemoryResponseBody body = new BatchDeleteUserVariableMemoryResponseBody();
        assertNotEquals(null, body);
    }

    @Test
    void testEquals_DifferentClass() {
        BatchDeleteUserVariableMemoryResponseBody body = new BatchDeleteUserVariableMemoryResponseBody();
        assertNotEquals("string", body);
    }

    @Test
    void testHashCode_Consistency() {
        BatchDeleteUserVariableMemoryResponseBody b1 = new BatchDeleteUserVariableMemoryResponseBody()
                .setVariableKeys(Arrays.asList("k"));
        BatchDeleteUserVariableMemoryResponseBody b2 = new BatchDeleteUserVariableMemoryResponseBody()
                .setVariableKeys(Arrays.asList("k"));
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        BatchDeleteUserVariableMemoryResponseBody body = new BatchDeleteUserVariableMemoryResponseBody()
                .setVariableKeys(Arrays.asList("k"));
        assertNotNull(body.toString());
    }
}
