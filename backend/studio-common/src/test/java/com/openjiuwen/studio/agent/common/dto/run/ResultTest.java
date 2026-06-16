/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void testGettersAndSetters_AllFields() {
        List<WordInfo> wordInfoList = new ArrayList<>();
        Result result = new Result()
                .setText("hello")
                .setScore(0.95f)
                .setWordInfo(wordInfoList);

        assertEquals("hello", result.getText());
        assertEquals(0.95f, result.getScore());
        assertSame(wordInfoList, result.getWordInfo());
    }

    @Test
    void testSetters_ReturnThis() {
        Result result = new Result();
        assertSame(result, result.setText("text"));
        assertSame(result, result.setScore(1.0f));
        assertSame(result, result.setWordInfo(new ArrayList<>()));
    }

    @Test
    void testEquals_SameObject() {
        Result result = new Result().setText("text").setScore(0.5f);
        assertEquals(result, result);
    }

    @Test
    void testEquals_EqualObject() {
        Result result1 = new Result().setText("text").setScore(0.5f).setWordInfo(null);
        Result result2 = new Result().setText("text").setScore(0.5f).setWordInfo(null);
        assertEquals(result1, result2);
    }

    @Test
    void testEquals_Null() {
        Result result = new Result().setText("text");
        assertNotEquals(null, result);
    }

    @Test
    void testEquals_DifferentClass() {
        Result result = new Result().setText("text");
        assertNotEquals("string", result);
    }

    @Test
    void testHashCode_Consistency() {
        Result result1 = new Result().setText("text").setScore(0.5f);
        Result result2 = new Result().setText("text").setScore(0.5f);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        Result result = new Result().setText("text").setScore(0.5f);
        assertNotNull(result.toString());
    }
}
