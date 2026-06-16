/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AsrConfigTest {

    @Test
    void testAllGettersSetters() {
        AsrConfig obj = new AsrConfig()
                .setAudioFormat("wav")
                .setProperty("zh-CN")
                .setAddPunc("yes")
                .setDigitNorm("no")
                .setVocabularyId("vocab-001")
                .setNeedWordInfo("true");
        assertEquals("wav", obj.getAudioFormat());
        assertEquals("zh-CN", obj.getProperty());
        assertEquals("yes", obj.getAddPunc());
        assertEquals("no", obj.getDigitNorm());
        assertEquals("vocab-001", obj.getVocabularyId());
        assertEquals("true", obj.getNeedWordInfo());
    }

    @Test
    void testEquals_SameObject() {
        AsrConfig obj = new AsrConfig().setAudioFormat("wav");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        AsrConfig obj1 = new AsrConfig()
                .setAudioFormat("wav").setProperty("zh-CN")
                .setAddPunc("yes").setDigitNorm("no")
                .setVocabularyId("v1").setNeedWordInfo("true");
        AsrConfig obj2 = new AsrConfig()
                .setAudioFormat("wav").setProperty("zh-CN")
                .setAddPunc("yes").setDigitNorm("no")
                .setVocabularyId("v1").setNeedWordInfo("true");
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        AsrConfig obj = new AsrConfig().setAudioFormat("wav");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        AsrConfig obj = new AsrConfig().setAudioFormat("wav");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        AsrConfig obj1 = new AsrConfig().setAudioFormat("wav");
        AsrConfig obj2 = new AsrConfig().setAudioFormat("mp3");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        AsrConfig obj1 = new AsrConfig().setAudioFormat("wav").setProperty("zh-CN");
        AsrConfig obj2 = new AsrConfig().setAudioFormat("wav").setProperty("zh-CN");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AsrConfig obj = new AsrConfig().setAudioFormat("wav");
        assertNotNull(obj.toString());
    }
}
