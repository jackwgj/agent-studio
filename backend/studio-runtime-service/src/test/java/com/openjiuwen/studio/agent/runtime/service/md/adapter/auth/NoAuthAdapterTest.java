/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.auth;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

class NoAuthAdapterTest {
    @Test
    public void test() {
        new NoAuthAdapter().requestHeaderHandler("", "", new HashMap<>(), new HashMap<>(), null, null);
    }
}
