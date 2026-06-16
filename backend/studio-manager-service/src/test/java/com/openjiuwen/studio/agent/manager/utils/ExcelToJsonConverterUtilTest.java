/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.utils;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.StructMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExcelToJsonConverterUtilTest {

    @Test
    void testIsExcelFile_Xlsx() {
        assertTrue(ExcelToJsonConverterUtil.isExcelFile(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test.xlsx"));
    }

    @Test
    void testIsExcelFile_Xls() {
        assertTrue(ExcelToJsonConverterUtil.isExcelFile("application/vnd.ms-excel", "test.xls"));
    }

    @Test
    void testIsExcelFile_NullFilename() {
        assertFalse(ExcelToJsonConverterUtil.isExcelFile("application/vnd.ms-excel", null));
    }

    @Test
    void testIsExcelFile_NonExcelFile() {
        assertFalse(ExcelToJsonConverterUtil.isExcelFile("text/plain", "test.txt"));
    }

    @Test
    void testIsExcelFile_ByContentTypeOnly() {
        assertTrue(ExcelToJsonConverterUtil.isExcelFile("application/vnd.ms-excel", "unknown"));
    }

    @Test
    void testIsExcelFile_XlsxContentType() {
        assertTrue(ExcelToJsonConverterUtil.isExcelFile(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "noext"));
    }

    @Test
    void testPreCheckExcelFile_NullFile() {
        assertThrows(AgentStudioException.class, () -> ExcelToJsonConverterUtil.preCheckExcelFile(null));
    }

    @Test
    void testPreCheckExcelFile_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        assertThrows(AgentStudioException.class, () -> ExcelToJsonConverterUtil.preCheckExcelFile(emptyFile));
    }

    @Test
    void testPreCheckExcelFile_NonExcelType() {
        MockMultipartFile txtFile = new MockMultipartFile("file", "test.txt",
            "text/plain", "hello".getBytes());

        assertThrows(AgentStudioException.class, () -> ExcelToJsonConverterUtil.preCheckExcelFile(txtFile));
    }

    @Test
    void testExportMessagesToExcel_NullMessages() {
        assertThrows(AgentStudioException.class, () -> ExcelToJsonConverterUtil.exportMessagesToExcel(null));
    }

    @Test
    void testExportMessagesToExcel_TooManyMessages() {
        List<StructMessage> messages = new ArrayList<>();
        for (int i = 0; i < 202; i++) {
            StructMessage msg = new StructMessage();
            msg.setName("msg-" + i);
            messages.add(msg);
        }

        assertThrows(AgentStudioException.class, () -> ExcelToJsonConverterUtil.exportMessagesToExcel(messages));
    }

    @Test
    void testExportMessagesToExcel_EmptyList() throws IOException {
        List<StructMessage> messages = Collections.emptyList();

        Resource result = ExcelToJsonConverterUtil.exportMessagesToExcel(messages);

        assertNotNull(result);
        assertNotNull(result.getFilename());
        assertTrue(result.getFilename().endsWith(".xlsx"));
    }

    @Test
    void testExportMessagesToExcel_WithData() throws IOException {
        List<StructMessage> messages = new ArrayList<>();
        StructMessage msg = new StructMessage();
        msg.setName("test-msg");
        msg.setCategory("异常");
        msg.setVisibility("personal");
        msg.setContent(Map.of("code", "ERR-001"));
        messages.add(msg);

        Resource result = ExcelToJsonConverterUtil.exportMessagesToExcel(messages);

        assertNotNull(result);
        assertTrue(result.getInputStream().readAllBytes().length > 0);
    }

    @Test
    void testExportStructuredMessagesTemplate_Normal() {
        Resource result = ExcelToJsonConverterUtil.exportStructuredMessagesTemplate("proj-1", "ws-1");

        assertNotNull(result);
        assertEquals("template.xlsx", result.getFilename());
    }

    @Test
    void testReverseVisibilityMap_ContainsExpectedMappings() {
        Map<String, String> reverseMap = ExcelToJsonConverterUtil.REVERSE_VISIBILITY_MAP;

        assertNotNull(reverseMap);
        assertEquals("仅个人", reverseMap.get("personal"));
        assertEquals("租户内", reverseMap.get("tenant"));
        assertEquals("当前空间内", reverseMap.get("workspace"));
    }
}
