/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ModelServiceInvokeDataTest {

    @Test
    void testSetGetData() {
        ModelServiceInvokeData invokeData = new ModelServiceInvokeData();
        assertNull(invokeData.getData());
        ModelServiceCheckReq req = new ModelServiceCheckReq();
        List<ModelServiceCheckReq> data = List.of(req);
        ModelServiceInvokeData result = invokeData.setData(data);
        assertSame(invokeData, result);
        assertEquals(data, invokeData.getData());
    }

    @Test
    void testChainedSetters() {
        ModelServiceCheckReq req = new ModelServiceCheckReq()
                .setModelType("llm")
                .setApiUrl("http://api.example.com")
                .setAuthType("token")
                .setModelName("gpt-4");
        ModelServiceInvokeData invokeData = new ModelServiceInvokeData()
                .setData(List.of(req));
        assertEquals(1, invokeData.getData().size());
        assertEquals("llm", invokeData.getData().get(0).getModelType());
    }

    @Test
    void testNoArgsConstructor() {
        ModelServiceInvokeData invokeData = new ModelServiceInvokeData();
        assertNotNull(invokeData);
        assertNull(invokeData.getData());
    }
}
