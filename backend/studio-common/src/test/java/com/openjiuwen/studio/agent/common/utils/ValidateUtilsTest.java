/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ValidateUtilsTest {

    @Test
    public void testIsEmptyString_Null() {
        assertTrue(ValidateUtils.isEmptyString(null));
    }

    @Test
    public void testIsEmptyString_Empty() {
        assertTrue(ValidateUtils.isEmptyString(""));
    }

    @Test
    public void testIsEmptyString_NonEmpty() {
        assertFalse(ValidateUtils.isEmptyString("hello"));
    }

    @Test
    public void testIsEmptyString_Blank() {
        assertFalse(ValidateUtils.isEmptyString(" "));
    }

    @Test
    public void testIsNotEmptyString_Null() {
        assertFalse(ValidateUtils.isNotEmptyString(null));
    }

    @Test
    public void testIsNotEmptyString_Empty() {
        assertFalse(ValidateUtils.isNotEmptyString(""));
    }

    @Test
    public void testIsNotEmptyString_NonEmpty() {
        assertTrue(ValidateUtils.isNotEmptyString("hello"));
    }

    @Test
    public void testIsNotEmptyString_Blank() {
        assertTrue(ValidateUtils.isNotEmptyString(" "));
    }

    @Test
    public void testIsEmptyCollection_Null() {
        assertTrue(ValidateUtils.isEmptyCollection(null));
    }

    @Test
    public void testIsEmptyCollection_Empty() {
        assertTrue(ValidateUtils.isEmptyCollection(new ArrayList<>()));
    }

    @Test
    public void testIsEmptyCollection_NonEmpty() {
        assertFalse(ValidateUtils.isEmptyCollection(List.of("a")));
    }

    @Test
    public void testIsNotEmptyCollection_Null() {
        assertFalse(ValidateUtils.isNotEmptyCollection(null));
    }

    @Test
    public void testIsNotEmptyCollection_Empty() {
        assertFalse(ValidateUtils.isNotEmptyCollection(new ArrayList<>()));
    }

    @Test
    public void testIsNotEmptyCollection_NonEmpty() {
        assertTrue(ValidateUtils.isNotEmptyCollection(List.of("a")));
    }

    @Test
    public void testIsEmptyCollection_MultipleElements() {
        assertFalse(ValidateUtils.isEmptyCollection(List.of("a", "b", "c")));
    }
}
