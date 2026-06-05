/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.openjiuwen.studio.agent.common.utils.FileCommonUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@MockitoSettings(strictness = Strictness.LENIENT)
class FileCommonUtilsTest {
    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_convertFileToMultipartFile_should_return_not_null() throws Exception {
        // Given
        File file = new File("test");

        // When
        MultipartFile result = FileCommonUtils.convertFileToMultipartFile(file, "test", "file");

        result.getName();
        result.getOriginalFilename();
        result.getContentType();
        result.isEmpty();
        result.getSize();

        // Then
        assertNotNull(result);
    }
}
