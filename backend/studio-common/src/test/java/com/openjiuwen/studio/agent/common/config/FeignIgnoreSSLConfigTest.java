/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.config;

import feign.Client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FeignIgnoreSSLConfigTest {

    @Test
    void testFeignClient() {
        FeignIgnoreSSLConfig config = new FeignIgnoreSSLConfig();
        Client client = config.feignClient();
        assertNotNull(client);
    }
}
