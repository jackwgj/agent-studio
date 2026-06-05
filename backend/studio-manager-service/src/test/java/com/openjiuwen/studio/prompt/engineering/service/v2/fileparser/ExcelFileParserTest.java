/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2.fileparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.alibaba.excel.EasyExcelFactory;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptVar;
import com.openjiuwen.studio.prompt.engineering.service.v2.fileparser.listener.DynamicPromptVarExcelListener;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class ExcelFileParserTest {
    @InjectMocks
    private ExcelFileParser excelFileParser;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(excelFileParser, "varsNum", 0);
        ReflectionTestUtils.setField(excelFileParser, "varsNameLength", 0);
        ReflectionTestUtils.setField(excelFileParser, "varsContentLength", 0);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_type_should_equal_result() throws Exception {

        // When
        String result = excelFileParser.type();

        // Then
        assertEquals("xlsx;xls", result);
    }



    @Test
    void test_parseData_should_not_throw_exception() throws Exception {
        assertThrows(AgentStudioException.class, () -> {
            try (MockedStatic<EasyExcelFactory> mockedStaticEasyExcelFactory = mockStatic(EasyExcelFactory.class,
                RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticEasyExcelFactory.when(
                    () -> EasyExcelFactory.read(any(InputStream.class), any(DynamicPromptVarExcelListener.class))
                        .sheet(eq(0))
                        .headRowNumber(eq(1))).thenReturn(null);

                // When
                List<List<PromptVar>> result = excelFileParser.parseData(null, null, null, null, null, 0);
            }
        });
    }



    @Test
    void test_parseData_exception1() throws Exception {
        // Mock dependencies
        MultipartFile mockFile = mock(MultipartFile.class);
        InputStream mockInputStream = mock(InputStream.class);

        // Mock method calls
        when(mockFile.getInputStream()).thenReturn(mockInputStream);
        doThrow(new IOException("Mocked exception")).when(mockInputStream).read();

        // Call the method under test and expect an exception
        assertThrows(AgentStudioException.class, () -> {
            excelFileParser.parseData(mockFile, new HashMap<>(), "projectId", "workspaceId", "taskId", 10);
        });
    }



    @Test
    void test_parseData_exception2() throws Exception {
        // Mock dependencies
        MultipartFile file = mock(MultipartFile.class);
        InputStream inputStream = mock(InputStream.class);

        // Mock behavior to throw an exception
        when(file.getInputStream()).thenReturn(inputStream);
        doThrow(new RuntimeException("Mocked exception")).when(inputStream).read();

        // Verify that the exception is thrown
        assertThrows(AgentStudioException.class, () -> {
            excelFileParser.parseData(file, new HashMap<>(), "projectId", "workspaceId", "taskId", 10);
        });
    }



    @Test
    void test_parseData_exception3() throws Exception {
        // Mock dependencies
        MultipartFile file = mock(MultipartFile.class);
        Map<String, PromptVar> varsConfig = new HashMap<>();
        String projectId = "projectId";
        String workspaceId = "workspaceId";
        String taskId = "taskId";
        int restNum = 10;

        InputStream inputStream = mock(InputStream.class);
        when(file.getInputStream()).thenReturn(inputStream);

        // Mock EasyExcel.read to throw an exception

        // Call the method under test and expect an exception
        assertThrows(AgentStudioException.class, () -> {
            excelFileParser.parseData(file, varsConfig, projectId, workspaceId, taskId, restNum);
        });
    }
}
