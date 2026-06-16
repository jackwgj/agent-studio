/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumUtilTest {

    enum TestEnum implements IEnum<String> {
        A("a"),
        B("b"),
        C("c");

        private final String value;

        TestEnum(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    @Test
    void testValueOf_Found() {
        TestEnum result = EnumUtil.valueOf(TestEnum.class, "a");
        assertEquals(TestEnum.A, result);
    }

    @Test
    void testValueOf_NotFound() {
        TestEnum result = EnumUtil.valueOf(TestEnum.class, "z");
        assertNull(result);
    }

    @Test
    void testNameOf_Found() {
        TestEnum result = EnumUtil.nameOf(TestEnum.class, "B");
        assertEquals(TestEnum.B, result);
    }

    @Test
    void testNameOf_NotFound() {
        TestEnum result = EnumUtil.nameOf(TestEnum.class, "Z");
        assertNull(result);
    }

    @Test
    void testValueOf_NullValue() {
        assertThrows(NullPointerException.class, () -> EnumUtil.valueOf(TestEnum.class, null));
    }

    @Test
    void testNameOf_NullName() {
        assertThrows(NullPointerException.class, () -> EnumUtil.nameOf(TestEnum.class, null));
    }
}
