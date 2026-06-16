/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.tool;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class RunToolResponseBodyTest {

    @Test
    void testAllGettersSetters() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        RunToolResponseBody obj = new RunToolResponseBody()
                .setRawRequestUrl("http://example.com/api")
                .setRawRequestHeaders(headers)
                .setRawRequestBody("{\"key\":\"value\"}")
                .setRawResponseCode(200)
                .setRawResponse("{\"result\":\"ok\"}")
                .setSuccess(true)
                .setErrorMessage("none");
        assertEquals("http://example.com/api", obj.getRawRequestUrl());
        assertEquals(headers, obj.getRawRequestHeaders());
        assertEquals("{\"key\":\"value\"}", obj.getRawRequestBody());
        assertEquals(200, obj.getRawResponseCode());
        assertEquals("{\"result\":\"ok\"}", obj.getRawResponse());
        assertTrue(obj.isSuccess());
        assertEquals("none", obj.getErrorMessage());
    }

    @Test
    void testDefaultHeaders() {
        RunToolResponseBody obj = new RunToolResponseBody();
        assertNotNull(obj.getRawRequestHeaders());
        assertTrue(obj.getRawRequestHeaders().isEmpty());
    }

    @Test
    void testEquals_SameObject() {
        RunToolResponseBody obj = new RunToolResponseBody().setRawRequestUrl("http://example.com");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        Map<String, String> headers = Map.of("h1", "v1");
        RunToolResponseBody obj1 = new RunToolResponseBody()
                .setRawRequestUrl("http://api").setRawRequestHeaders(headers)
                .setRawRequestBody("body").setRawResponseCode(200)
                .setRawResponse("resp").setSuccess(true).setErrorMessage("none");
        RunToolResponseBody obj2 = new RunToolResponseBody()
                .setRawRequestUrl("http://api").setRawRequestHeaders(headers)
                .setRawRequestBody("body").setRawResponseCode(200)
                .setRawResponse("resp").setSuccess(true).setErrorMessage("none");
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        RunToolResponseBody obj = new RunToolResponseBody().setRawRequestUrl("http://example.com");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        RunToolResponseBody obj = new RunToolResponseBody().setRawRequestUrl("http://example.com");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        RunToolResponseBody obj1 = new RunToolResponseBody().setRawRequestUrl("http://api1");
        RunToolResponseBody obj2 = new RunToolResponseBody().setRawRequestUrl("http://api2");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        RunToolResponseBody obj1 = new RunToolResponseBody()
                .setRawRequestUrl("http://api").setRawResponseCode(200);
        RunToolResponseBody obj2 = new RunToolResponseBody()
                .setRawRequestUrl("http://api").setRawResponseCode(200);
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        RunToolResponseBody obj = new RunToolResponseBody().setRawRequestUrl("http://example.com");
        assertNotNull(obj.toString());
    }
}
