/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class ErrorRspTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        ErrorDetail detail = new ErrorDetail().setErrorCode("D001").setErrorMsg("detail msg");
        ErrorRsp rsp = new ErrorRsp()
                .setErrorCode("E001")
                .setErrorMsg("error msg")
                .setErrorReason("bad input")
                .setErrorSuggestion("fix it")
                .setDetails(List.of(detail));
        assertEquals("E001", rsp.getErrorCode());
        assertEquals("error msg", rsp.getErrorMsg());
        assertEquals("bad input", rsp.getErrorReason());
        assertEquals("fix it", rsp.getErrorSuggestion());
        assertEquals(1, rsp.getDetails().size());
        assertEquals(detail, rsp.getDetails().get(0));
    }

    @Test
    void testEquals_SameInstance() {
        ErrorRsp rsp = new ErrorRsp().setErrorCode("E001");
        assertEquals(rsp, rsp);
    }

    @Test
    void testEquals_EqualObjects() {
        ErrorRsp r1 = new ErrorRsp().setErrorCode("E001").setErrorMsg("msg");
        ErrorRsp r2 = new ErrorRsp().setErrorCode("E001").setErrorMsg("msg");
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        ErrorRsp rsp = new ErrorRsp();
        assertNotEquals(null, rsp);
    }

    @Test
    void testEquals_DifferentClass() {
        ErrorRsp rsp = new ErrorRsp();
        assertNotEquals("string", rsp);
    }

    @Test
    void testHashCode_Consistency() {
        ErrorRsp r1 = new ErrorRsp().setErrorCode("E001").setErrorMsg("msg");
        ErrorRsp r2 = new ErrorRsp().setErrorCode("E001").setErrorMsg("msg");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ErrorRsp rsp = new ErrorRsp().setErrorCode("E001");
        assertNotNull(rsp.toString());
    }
}
