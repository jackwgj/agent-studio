/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.filter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

public class RuntimeModelRouterFilterConfigTest {
    @Test
    public void test() {
        FilterRegistrationBean<RuntimeContextFilter> bean
            = new RuntimeModelRouterFilterConfig().createRuntimeContextFilter();
        Assertions.assertNotNull(bean);
    }
}
