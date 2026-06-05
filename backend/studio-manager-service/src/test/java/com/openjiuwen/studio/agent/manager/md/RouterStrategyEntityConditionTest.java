/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.md;

import com.openjiuwen.studio.agent.manager.entity.md.RouterStrategyEntityCondition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RouterStrategyEntityConditionTest {
    @Test
    public void sqlEscapeCharTest() {
        RouterStrategyEntityCondition condition = new RouterStrategyEntityCondition();
        condition.setQuery(" ");
        condition.sqlEscapeChar();
        Assertions.assertNull(condition.getQuery());

        condition.setQuery("aa_ff");
        condition.sqlEscapeChar();
        Assertions.assertEquals("aa\\_ff", condition.getQuery());
    }
}
