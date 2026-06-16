/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Base64;

@MockitoSettings(strictness = Strictness.LENIENT)
class ImageBase64UtilsTest {

    @Test
    void testIsValidBase64Image_WithValidPng() {
        String dataUri = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        assertTrue(ImageBase64Utils.isValidBase64Image(dataUri));
    }

    @Test
    void testIsValidBase64Image_WithValidJpeg() {
        String dataUri = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFAABAAAAAAAAAAAAAAAAAAAACf/EABQQAQAAAAAAAAAAAAAAAAAAAAD/xAAUAQEAAAAAAAAAAAAAAAAAAAAA/8QAFBEBAAAAAAAAAAAAAAAAAAAAAP/aAAwDAQACEQMRAD8AKwA//9k=";
        assertTrue(ImageBase64Utils.isValidBase64Image(dataUri));
    }

    @Test
    void testIsValidBase64Image_WithValidSvg() {
        String dataUri = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjwvc3ZnPg==";
        assertTrue(ImageBase64Utils.isValidBase64Image(dataUri));
    }

    @Test
    void testIsValidBase64Image_WithInvalidFormat() {
        assertFalse(ImageBase64Utils.isValidBase64Image("not-a-base64-image"));
    }

    @Test
    void testIsValidBase64Image_WithGifFormat() {
        assertFalse(ImageBase64Utils.isValidBase64Image("data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7"));
    }

    @Test
    void testIsValidBase64Image_WithEmptyString() {
        assertFalse(ImageBase64Utils.isValidBase64Image(""));
    }

    @Test
    void testValidateImage_WithBlankInput() {
        assertThrows(AgentStudioException.class, () -> ImageBase64Utils.validateImage(""));
    }

    @Test
    void testValidateImage_WithNullInput() {
        assertThrows(AgentStudioException.class, () -> ImageBase64Utils.validateImage(null));
    }

    @Test
    void testValidateImage_WithInvalidFormat() {
        assertThrows(AgentStudioException.class, () -> ImageBase64Utils.validateImage("invalid-image-data"));
    }

    @Test
    void testValidateImage_WithValidPngDataUri() {
        String base64Data = Base64.getEncoder().encodeToString(new byte[100]);
        String dataUri = "data:image/png;base64," + base64Data;
        ImageBase64Utils.validateImage(dataUri, 1024);
    }

    @Test
    void testValidateImage_WithRawBase64() {
        String base64Data = Base64.getEncoder().encodeToString(new byte[100]);
        ImageBase64Utils.validateImage(base64Data, 1024);
    }

    @Test
    void testValidateImage_DefaultLimit() {
        String base64Data = Base64.getEncoder().encodeToString(new byte[100]);
        String dataUri = "data:image/png;base64," + base64Data;
        ImageBase64Utils.validateImage(dataUri);
    }

    @Test
    void testGetImageBase64_WithNullIconName() {
        MgObsService obsService = mock(MgObsService.class);
        assertThrows(AgentStudioException.class,
            () -> ImageBase64Utils.getImageBase64(null, obsService));
    }

    @Test
    void testGetImageBase64_WithNullObsService() {
        assertThrows(AgentStudioException.class,
            () -> ImageBase64Utils.getImageBase64("icon.png", null));
    }

    @Test
    void testGetImageBase64_WithValidPng() {
        MgObsService obsService = mock(MgObsService.class);
        String base64Content = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3});
        when(obsService.downloadObsImageFile("test.png")).thenReturn(base64Content);
        String result = ImageBase64Utils.getImageBase64("test.png", obsService);
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }

    @Test
    void testGetImageBase64_WithJpegExtension() {
        MgObsService obsService = mock(MgObsService.class);
        String base64Content = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3});
        when(obsService.downloadObsImageFile("test.jpeg")).thenReturn(base64Content);
        String result = ImageBase64Utils.getImageBase64("test.jpeg", obsService);
        assertTrue(result.startsWith("data:image/jpeg;base64,"));
    }

    @Test
    void testGetImageBase64_WithObsException() {
        MgObsService obsService = mock(MgObsService.class);
        when(obsService.downloadObsImageFile("test.png")).thenThrow(new RuntimeException("OBS error"));
        assertThrows(AgentStudioException.class,
            () -> ImageBase64Utils.getImageBase64("test.png", obsService));
    }
}
