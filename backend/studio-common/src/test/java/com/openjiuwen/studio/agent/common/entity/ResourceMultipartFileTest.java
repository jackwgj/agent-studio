/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.entity.ResourceMultipartFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;

@MockitoSettings(strictness = Strictness.LENIENT)
class ResourceMultipartFileTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Resource resource;

    @InjectMocks
    private ResourceMultipartFile resourceMultipartFile;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(resourceMultipartFile, "resource", resource);
        ReflectionTestUtils.setField(resourceMultipartFile, "fileName", "test");
        ReflectionTestUtils.setField(resourceMultipartFile, "contentType", "test");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_getName_should_equal_result() throws Exception {

        // When
        String result = resourceMultipartFile.getName();

        // Then
        assertEquals("file", result);
    }

    @Test
    void test_getOriginalFilename_should_equal_result() throws Exception {

        // When
        String result = resourceMultipartFile.getOriginalFilename();

        // Then
        assertEquals("test", result);
    }

    @Test
    void test_getContentType_should_equal_result() throws Exception {

        // When
        String result = resourceMultipartFile.getContentType();

        // Then
        assertEquals("test", result);
    }

    @Test
    void test_isEmpty_should_return_true() throws Exception {
        // Given
        when(resource.contentLength()).thenReturn(0L);

        // When
        boolean result = resourceMultipartFile.isEmpty();

        // Then
        assertTrue(result);
    }

    @Test
    void test_getSize_should_equal_result() throws Exception {
        // Given
        when(resource.contentLength()).thenReturn(0L);

        // When
        long result = resourceMultipartFile.getSize();

        // Then
        assertEquals(0L, result);
    }

    @Test
    void test_getBytes_should_return_not_null() throws Exception {
        // Given
        InputStream is = mock(InputStream.class, Answers.RETURNS_DEEP_STUBS);
        when(is.read(any(byte[].class))).thenReturn(-1);
        when(resource.getInputStream()).thenReturn(is);

        // When
        byte[] result = resourceMultipartFile.getBytes();

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getInputStream_should_return_not_null() throws Exception {
        // Given
        InputStream inputStream = mock(InputStream.class, Answers.RETURNS_DEEP_STUBS);
        when(resource.getInputStream()).thenReturn(inputStream);

        // When
        InputStream result = resourceMultipartFile.getInputStream();

        // Then
        assertNotNull(result);
    }
}
