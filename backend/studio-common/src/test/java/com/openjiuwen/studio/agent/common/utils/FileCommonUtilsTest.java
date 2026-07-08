/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FileCommonUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void testConvertFileToMultipartFile_AllMethods() throws IOException {
        Path filePath = tempDir.resolve("test.json");
        Files.writeString(filePath, "{\"key\":\"value\"}");
        File file = filePath.toFile();

        MultipartFile multipart = FileCommonUtils.convertFileToMultipartFile(file, "application/json", "fileField");

        assertEquals("fileField", multipart.getName());
        assertEquals("test.json", multipart.getOriginalFilename());
        assertEquals("application/json", multipart.getContentType());
        assertFalse(multipart.isEmpty());
        assertTrue(multipart.getSize() > 0);
        assertNotNull(multipart.getBytes());
        try (var is = multipart.getInputStream()) {
            assertNotNull(is);
        }
    }

    @Test
    void testConvertFileToMultipartFile_TransferTo() throws IOException {
        Path filePath = tempDir.resolve("source.txt");
        Files.writeString(filePath, "hello");
        File file = filePath.toFile();

        MultipartFile multipart = FileCommonUtils.convertFileToMultipartFile(file, "text/plain", "f");
        File dest = tempDir.resolve("dest.txt").toFile();
        multipart.transferTo(dest);

        assertTrue(dest.exists());
        assertEquals("hello", Files.readString(dest.toPath()));
    }

    @Test
    void testConvertFileToMultipartFile_EmptyFile() throws IOException {
        Path filePath = tempDir.resolve("empty.txt");
        Files.writeString(filePath, "");
        File file = filePath.toFile();

        MultipartFile multipart = FileCommonUtils.convertFileToMultipartFile(file, "text/plain", "f");

        assertTrue(multipart.isEmpty());
        assertEquals(0, multipart.getSize());
    }

    @Test
    void testValidatedFile_NullFile() {
        assertThrows(AgentStudioException.class,
            () -> FileCommonUtils.validatedFile(null, 1024, ".json"));
    }

    @Test
    void testValidatedFile_EmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(AgentStudioException.class,
            () -> FileCommonUtils.validatedFile(file, 1024, ".json"));
    }

    @Test
    void testValidatedFile_OversizedFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(2048L);

        assertThrows(AgentStudioException.class,
            () -> FileCommonUtils.validatedFile(file, 1024, ".json"));
    }

    @Test
    void testValidatedFile_InvalidFilename_PathTraversal() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("..secret.json");

        assertThrows(AgentStudioException.class,
            () -> FileCommonUtils.validatedFile(file, 1024, ".json"));
    }

    @Test
    void testValidatedFile_InvalidFilename_Backslash() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("path\\file.json");

        assertThrows(AgentStudioException.class,
            () -> FileCommonUtils.validatedFile(file, 1024, ".json"));
    }

    @Test
    void testValidatedFile_InvalidFilename_Slash() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("path/file.json");

        assertThrows(AgentStudioException.class,
            () -> FileCommonUtils.validatedFile(file, 1024, ".json"));
    }

    @Test
    void testValidatedFile_InvalidFilename_NoExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("noextension");

        assertThrows(AgentStudioException.class,
            () -> FileCommonUtils.validatedFile(file, 1024, ".json"));
    }

    @Test
    void testValidatedFile_InvalidFilename_Blank() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("");

        assertThrows(AgentStudioException.class,
            () -> FileCommonUtils.validatedFile(file, 1024, ".json"));
    }

    @Test
    void testValidatedFile_WrongFileType() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("data.xml");

        assertThrows(AgentStudioException.class,
            () -> FileCommonUtils.validatedFile(file, 1024, ".json"));
    }

    @Test
    void testValidatedFile_ValidFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("data.json");

        assertDoesNotThrow(() -> FileCommonUtils.validatedFile(file, 1024, ".json"));
    }

    @Test
    void testValidatedFile_CaseInsensitiveType() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("data.JSON");

        assertDoesNotThrow(() -> FileCommonUtils.validatedFile(file, 1024, ".json"));
    }
}
