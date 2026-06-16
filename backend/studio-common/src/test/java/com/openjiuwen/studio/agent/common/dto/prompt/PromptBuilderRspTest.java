/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class PromptBuilderRspTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        PromptBuilderRsp rsp = new PromptBuilderRsp()
                .setCode(200)
                .setMessage("success")
                .setData("response data");
        assertEquals(200, rsp.getCode());
        assertEquals("success", rsp.getMessage());
        assertEquals("response data", rsp.getData());
    }

    @Test
    void testEquals_SameInstance() {
        PromptBuilderRsp rsp = new PromptBuilderRsp().setCode(200);
        assertEquals(rsp, rsp);
    }

    @Test
    void testEquals_EqualObjects() {
        PromptBuilderRsp r1 = new PromptBuilderRsp().setCode(200).setMessage("ok");
        PromptBuilderRsp r2 = new PromptBuilderRsp().setCode(200).setMessage("ok");
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        PromptBuilderRsp rsp = new PromptBuilderRsp();
        assertNotEquals(null, rsp);
    }

    @Test
    void testEquals_DifferentClass() {
        PromptBuilderRsp rsp = new PromptBuilderRsp();
        assertNotEquals("string", rsp);
    }

    @Test
    void testHashCode_Consistency() {
        PromptBuilderRsp r1 = new PromptBuilderRsp().setCode(200).setMessage("ok");
        PromptBuilderRsp r2 = new PromptBuilderRsp().setCode(200).setMessage("ok");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        PromptBuilderRsp rsp = new PromptBuilderRsp().setCode(200);
        assertNotNull(rsp.toString());
    }
}
