/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * SourceType / Source / FileType 枚举单元测试
 */
class SourceEnumsTest {

    // ========== SourceType ==========

    @Test
    void testSourceType_History() {
        assertEquals(0, SourceType.HISTORY.getType());
    }

    @Test
    void testSourceType_Candidate() {
        assertEquals(1, SourceType.CANDIDATE.getType());
    }

    @Test
    void testSourceType_AllValues() {
        SourceType[] values = SourceType.values();
        assertEquals(2, values.length);
        assertEquals(SourceType.HISTORY, values[0]);
        assertEquals(SourceType.CANDIDATE, values[1]);
    }

    @Test
    void testSourceType_ValueOf() {
        assertEquals(SourceType.HISTORY, SourceType.valueOf("HISTORY"));
        assertEquals(SourceType.CANDIDATE, SourceType.valueOf("CANDIDATE"));
    }

    // ========== Source ==========

    @Test
    void testSource_AllValues() {
        Source[] values = Source.values();
        assertEquals(6, values.length);
    }

    @Test
    void testSource_ValueOf() {
        assertEquals(Source.PLAYGROUND, Source.valueOf("PLAYGROUND"));
        assertEquals(Source.EXPERIMENT, Source.valueOf("EXPERIMENT"));
        assertEquals(Source.CANDIDATE, Source.valueOf("CANDIDATE"));
        assertEquals(Source.HORIZONTAL, Source.valueOf("HORIZONTAL"));
        assertEquals(Source.NO_SOURCE, Source.valueOf("NO_SOURCE"));
        assertEquals(Source.PRESET, Source.valueOf("PRESET"));
    }

    @Test
    void testSource_Name() {
        assertEquals("PLAYGROUND", Source.PLAYGROUND.name());
        assertEquals("NO_SOURCE", Source.NO_SOURCE.name());
        assertEquals("PRESET", Source.PRESET.name());
    }

    // ========== FileType ==========

    @Test
    void testFileType_Excel() {
        assertEquals("excel", FileType.FILE_TYPE_EXCEL.getFileType());
    }

    @Test
    void testFileType_Image() {
        assertEquals("image", FileType.IMAGE.getFileType());
    }

    @Test
    void testFileType_Json() {
        assertEquals("json", FileType.JSON.getFileType());
    }

    @Test
    void testFileType_AllValues() {
        FileType[] values = FileType.values();
        assertEquals(3, values.length);
    }

    @Test
    void testFileType_ValueOf() {
        assertEquals(FileType.FILE_TYPE_EXCEL, FileType.valueOf("FILE_TYPE_EXCEL"));
        assertEquals(FileType.IMAGE, FileType.valueOf("IMAGE"));
        assertEquals(FileType.JSON, FileType.valueOf("JSON"));
    }
}
