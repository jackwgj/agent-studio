/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class FileCommonUtilsTest {

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
