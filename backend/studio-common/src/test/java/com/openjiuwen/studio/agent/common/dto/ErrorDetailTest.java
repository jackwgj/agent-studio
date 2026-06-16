/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorDetailTest {

    @Test
    void testGettersAndSetters_AllFields() {
        ErrorDetail detail = new ErrorDetail()
                .setErrorCode("ERR001")
                .setErrorMsg("something went wrong");

        assertEquals("ERR001", detail.getErrorCode());
        assertEquals("something went wrong", detail.getErrorMsg());
    }

    @Test
    void testSetters_ReturnThis() {
        ErrorDetail detail = new ErrorDetail();
        assertSame(detail, detail.setErrorCode("code"));
        assertSame(detail, detail.setErrorMsg("msg"));
    }

    @Test
    void testEquals_SameObject() {
        ErrorDetail detail = new ErrorDetail().setErrorCode("code");
        assertEquals(detail, detail);
    }

    @Test
    void testEquals_EqualObject() {
        ErrorDetail d1 = new ErrorDetail().setErrorCode("code").setErrorMsg("msg");
        ErrorDetail d2 = new ErrorDetail().setErrorCode("code").setErrorMsg("msg");
        assertEquals(d1, d2);
    }

    @Test
    void testEquals_Null() {
        ErrorDetail detail = new ErrorDetail();
        assertNotEquals(null, detail);
    }

    @Test
    void testEquals_DifferentClass() {
        ErrorDetail detail = new ErrorDetail();
        assertNotEquals("string", detail);
    }

    @Test
    void testHashCode_Consistency() {
        ErrorDetail d1 = new ErrorDetail().setErrorCode("code").setErrorMsg("msg");
        ErrorDetail d2 = new ErrorDetail().setErrorCode("code").setErrorMsg("msg");
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ErrorDetail detail = new ErrorDetail().setErrorCode("code");
        assertNotNull(detail.toString());
    }
}
