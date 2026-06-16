/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class FileExtractTest {

    @Test
    void testBuilder() {
        ParseConf parseConf = ParseConf.builder().ocrEnabled(true).build();
        SplitConf splitConf = SplitConf.builder().chunkSize(1000).build();
        FileExtract extract = FileExtract.builder()
                .parseConf(parseConf)
                .splitConf(splitConf)
                .build();
        assertEquals(parseConf, extract.getParseConf());
        assertEquals(splitConf, extract.getSplitConf());
    }

    @Test
    void testNoArgsConstructor() {
        FileExtract extract = new FileExtract();
        assertNotNull(extract);
        assertNull(extract.getParseConf());
        assertNull(extract.getSplitConf());
    }

    @Test
    void testAllArgsConstructor() {
        ParseConf parseConf = ParseConf.builder().build();
        SplitConf splitConf = SplitConf.builder().build();
        FileExtract extract = new FileExtract(parseConf, splitConf);
        assertEquals(parseConf, extract.getParseConf());
        assertEquals(splitConf, extract.getSplitConf());
    }

    @Test
    void testSettersAndGetters() {
        FileExtract extract = new FileExtract();
        ParseConf parseConf = ParseConf.builder().ocrEnabled(true).build();
        SplitConf splitConf = SplitConf.builder().chunkSize(500).build();
        extract.setParseConf(parseConf);
        extract.setSplitConf(splitConf);
        assertEquals(parseConf, extract.getParseConf());
        assertEquals(splitConf, extract.getSplitConf());
    }

    @Test
    void testEqualsAndHashCode() {
        ParseConf parseConf = ParseConf.builder().ocrEnabled(true).build();
        SplitConf splitConf = SplitConf.builder().chunkSize(500).build();
        FileExtract e1 = FileExtract.builder().parseConf(parseConf).splitConf(splitConf).build();
        FileExtract e2 = FileExtract.builder().parseConf(parseConf).splitConf(splitConf).build();
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testEquals_Different() {
        FileExtract e1 = FileExtract.builder().parseConf(ParseConf.builder().ocrEnabled(true).build()).build();
        FileExtract e2 = FileExtract.builder().parseConf(ParseConf.builder().ocrEnabled(false).build()).build();
        assertNotEquals(e1, e2);
    }

    @Test
    void testToString() {
        FileExtract extract = FileExtract.builder().build();
        assertNotNull(extract.toString());
    }

    @Test
    void testParseConf_Builder() {
        ParseConf conf = ParseConf.builder()
                .ocrEnabled(true)
                .imageEnabled(true)
                .headerFooterEnabled(true)
                .catalogEnabled(false)
                .imageConf(ParseConf.ImageConfEnum.TEXT)
                .build();
        assertEquals(true, conf.getOcrEnabled());
        assertEquals(true, conf.getImageEnabled());
        assertEquals(true, conf.getHeaderFooterEnabled());
        assertEquals(false, conf.getCatalogEnabled());
        assertEquals(ParseConf.ImageConfEnum.TEXT, conf.getImageConf());
    }

    @Test
    void testParseConf_DefaultValues() {
        ParseConf conf = new ParseConf();
        assertEquals(false, conf.getOcrEnabled());
        assertEquals(false, conf.getImageEnabled());
        assertEquals(false, conf.getHeaderFooterEnabled());
        assertNull(conf.getCatalogEnabled());
        assertNull(conf.getImageConf());
    }

    @Test
    void testParseConf_EqualsAndHashCode() {
        ParseConf c1 = ParseConf.builder().ocrEnabled(true).build();
        ParseConf c2 = ParseConf.builder().ocrEnabled(true).build();
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testImageConfEnum_Values() {
        ParseConf.ImageConfEnum[] values = ParseConf.ImageConfEnum.values();
        assertEquals(2, values.length);
        assertEquals(ParseConf.ImageConfEnum.TEXT, values[0]);
        assertEquals(ParseConf.ImageConfEnum.IMAGE, values[1]);
    }

    @Test
    void testImageConfEnum_FromValue() {
        assertEquals(ParseConf.ImageConfEnum.TEXT, ParseConf.ImageConfEnum.fromValue("TEXT"));
        assertEquals(ParseConf.ImageConfEnum.IMAGE, ParseConf.ImageConfEnum.fromValue("IMAGE"));
        assertNull(ParseConf.ImageConfEnum.fromValue("INVALID"));
    }

    @Test
    void testImageConfEnum_ToString() {
        assertEquals("TEXT", ParseConf.ImageConfEnum.TEXT.toString());
        assertEquals("IMAGE", ParseConf.ImageConfEnum.IMAGE.toString());
    }

    @Test
    void testSplitConf_Builder() {
        SplitConf conf = SplitConf.builder()
                .splitMode(SplitConf.SplitModeEnum.LENGTH)
                .chunkSize(1000)
                .titleLevel(5)
                .combineTitle(true)
                .mergeTitles(false)
                .ruleRegexId("regex-1")
                .build();
        assertEquals(SplitConf.SplitModeEnum.LENGTH, conf.getSplitMode());
        assertEquals(1000, conf.getChunkSize());
        assertEquals(5, conf.getTitleLevel());
        assertEquals(true, conf.getCombineTitle());
        assertEquals(false, conf.getMergeTitles());
        assertEquals("regex-1", conf.getRuleRegexId());
    }

    @Test
    void testSplitConf_DefaultValues() {
        SplitConf conf = new SplitConf();
        assertEquals(SplitConf.SplitModeEnum.AUTO, conf.getSplitMode());
        assertEquals(500, conf.getChunkSize());
        assertEquals(3, conf.getTitleLevel());
        assertNull(conf.getCombineTitle());
        assertNull(conf.getMergeTitles());
        assertNull(conf.getSeparatorIds());
        assertNull(conf.getRuleRegexId());
    }

    @Test
    void testSplitConf_EqualsAndHashCode() {
        SplitConf c1 = SplitConf.builder().chunkSize(500).build();
        SplitConf c2 = SplitConf.builder().chunkSize(500).build();
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testSplitModeEnum_Values() {
        SplitConf.SplitModeEnum[] values = SplitConf.SplitModeEnum.values();
        assertEquals(4, values.length);
    }

    @Test
    void testSplitModeEnum_FromValue() {
        assertEquals(SplitConf.SplitModeEnum.LENGTH, SplitConf.SplitModeEnum.fromValue("LENGTH"));
        assertEquals(SplitConf.SplitModeEnum.CATALOG, SplitConf.SplitModeEnum.fromValue("CATALOG"));
        assertEquals(SplitConf.SplitModeEnum.RULE, SplitConf.SplitModeEnum.fromValue("RULE"));
        assertEquals(SplitConf.SplitModeEnum.AUTO, SplitConf.SplitModeEnum.fromValue("AUTO"));
        assertNull(SplitConf.SplitModeEnum.fromValue("INVALID"));
    }
}
