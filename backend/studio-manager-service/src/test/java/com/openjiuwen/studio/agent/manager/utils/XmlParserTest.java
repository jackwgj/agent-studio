/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@MockitoSettings(strictness = Strictness.LENIENT)
class XmlParserTest {

    @Test
    void testXmlFileSuffix() {
        assertEquals(".xml", XmlParser.XML_FILE_SUFFIX);
    }

    @Test
    void testParseXml_WithEmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        assertThrows(AgentStudioException.class, () -> XmlParser.parseXml(file, 10));
    }

    @Test
    void testParseXml_WithOversizedFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(20L * 1024 * 1024);
        assertThrows(AgentStudioException.class, () -> XmlParser.parseXml(file, 10));
    }

    @Test
    void testParseXml_WithValidZipContainingXml() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("test.xml"));
            zos.write("<?xml version=\"1.0\"?><root><item>test</item></root>".getBytes());
            zos.closeEntry();
        }
        byte[] zipBytes = baos.toByteArray();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn((long) zipBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(zipBytes));

        XmlParser.parseXml(file, 10);
    }

    @Test
    void testParseXml_WithValidZipNoXml() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("test.txt"));
            zos.write("plain text content".getBytes());
            zos.closeEntry();
        }
        byte[] zipBytes = baos.toByteArray();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn((long) zipBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(zipBytes));

        XmlParser.parseXml(file, 10);
    }

    @Test
    void testParseXml_WithInvalidZip() throws Exception {
        byte[] invalidBytes = "not a zip file".getBytes();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn((long) invalidBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(invalidBytes));

        assertThrows(AgentStudioException.class, () -> XmlParser.parseXml(file, 10));
    }

    @Test
    void testParseXml_WithDirectoryEntry() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("dir/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("dir/test.xml"));
            zos.write("<?xml version=\"1.0\"?><root/>".getBytes());
            zos.closeEntry();
        }
        byte[] zipBytes = baos.toByteArray();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn((long) zipBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(zipBytes));

        XmlParser.parseXml(file, 10);
    }
}
