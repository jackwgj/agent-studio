/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow;

import com.openjiuwen.studio.agent.manager.utils.IRUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * IRUtilsTest
 *
 */
public class IRUtilsTest {
    @Test
    void test_toFloat() {
        // 待转换目标是null
        float result = IRUtils.toFloat(null, 0.5f);
        Assertions.assertEquals(0.5f, result);

        // 待转换目标是数值格式
        result = IRUtils.toFloat(new BigDecimal("0.31"), 0.5f);
        Assertions.assertEquals(0.31f, result);

        // 待转换目标是字符串格式
        result = IRUtils.toFloat("0.9", 0.5f);
        Assertions.assertEquals(0.9f, result);

        // 待转换目标是非法字符串
        result = IRUtils.toFloat("hello", 0.5f);
        Assertions.assertEquals(0.5f, result);

        // 待转换目标是其他格式
        result = IRUtils.toFloat(new Object(), 0.5f);
        Assertions.assertEquals(0.5f, result);
    }
}
