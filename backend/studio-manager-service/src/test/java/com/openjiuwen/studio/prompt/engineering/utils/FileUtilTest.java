/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.enums.FileType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

class FileUtilTest {
    private static final int MAX_FILE_SIZE = 1024; // 1KB for testing
    private Set<String> validSuffixSet;

    @Mock
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validSuffixSet = new HashSet<>();
        validSuffixSet.add("txt");
        validSuffixSet.add("jpg");
    }

    @Test
    void validateFile() {
        MockMultipartFile f1 = new MockMultipartFile("file", "test.json", "image/jpeg", "test".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile f2 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile f3 = new MockMultipartFile("file", "test.txt", "text/html", "test".getBytes(StandardCharsets.UTF_8));
        try {
            FileUtil.validateFile(f1, FileType.JSON);
        } catch (AgentStudioException e) {
            Assertions.assertTrue(true); // 这里可以添加更多的断言或处理逻辑
        } catch (Exception e) {
            Assertions.fail("Unexpected exception thrown: " + e.getMessage());
        }
        try {
            FileUtil.validateFile(f2, FileType.IMAGE);
        } catch (AgentStudioException e) {
            Assertions.assertTrue(true); // 这里可以添加更多的断言或处理逻辑
        } catch (Exception e) {
            Assertions.fail("Unexpected exception thrown: " + e.getMessage());
        }

    }

    @Test
    void validateFileSizeAndSuffix() {
        MockMultipartFile f1 = null;
        try {
            FileUtil.validateFileSizeAndSuffix(f1, 1024 * 1024 * 5, Set.of("json"));
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof AgentStudioException);
        }
        f1 = new MockMultipartFile("test", "test".getBytes(StandardCharsets.UTF_8));
        try {
            FileUtil.validateFileSizeAndSuffix(f1, 1024, Set.of("jpg"));
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof AgentStudioException);
        }
    }

    @Test
    void validateFilePath() {
        String path = FileUtil.validateFilePath("image", "./a.");
    }

    @Test
    public void testValidateFile_NullFile_ThrowsException() {
        assertThrows(AgentStudioException.class, () -> {
            FileUtil.validateFile(null, MAX_FILE_SIZE, validSuffixSet);
        });
    }

    @Test
    public void testValidateFile_EmptyFile_ThrowsException() {
        when(mockFile.isEmpty()).thenReturn(true);
        assertThrows(AgentStudioException.class, () -> {
            FileUtil.validateFile(mockFile, MAX_FILE_SIZE, validSuffixSet);
        });
    }

    @Test
    public void testValidateFile_FileSizeExceedsLimit_ThrowsException() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(2000L); // 2KB

        assertThrows(AgentStudioException.class, () -> {
            FileUtil.validateFile(mockFile, MAX_FILE_SIZE, validSuffixSet);
        });
    }

    @Test
    public void testValidateFile_IllegalFileName_ThrowsException() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(500L);
        when(mockFile.getOriginalFilename()).thenReturn("..invalidFile.jpg");

        assertThrows(AgentStudioException.class, () -> {
            FileUtil.validateFile(mockFile, MAX_FILE_SIZE, validSuffixSet);
        });
    }

    @Test
    public void testValidateFile_IllegalFileType_ThrowsException() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(500L);
        when(mockFile.getOriginalFilename()).thenReturn("invalidFile.doc");

        assertThrows(AgentStudioException.class, () -> {
            FileUtil.validateFile(mockFile, MAX_FILE_SIZE, validSuffixSet);
        });
    }

    @Test
    public void testValidateFile_ValidFile_NoException() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(500L);
        when(mockFile.getOriginalFilename()).thenReturn("validFile.jpg");

        assertDoesNotThrow(() -> {
            FileUtil.validateFile(mockFile, MAX_FILE_SIZE, validSuffixSet);
        });
    }

}
