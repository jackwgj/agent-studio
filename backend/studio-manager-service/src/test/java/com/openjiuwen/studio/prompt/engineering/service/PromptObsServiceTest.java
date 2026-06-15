/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * PromptObsService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PromptObsServiceTest {

    @Mock
    private ObsClient obsClient;

    @Mock
    private ObsObject obsObject;

    @InjectMocks
    private PromptObsService obsService;

    @BeforeEach
    void setUp() {
        obsService.setObsClient(obsClient);
        ReflectionTestUtils.setField(obsService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(obsService, "opensource", false);
    }

    // ========== upLoadImage (original) ==========

    @Test
    void upLoadImage() throws IOException {
        when(obsClient.doesObjectExist(anyString(), anyString())).thenReturn(true);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        when(obsObject.getMetadata()).thenReturn(objectMetadata);

        when(obsClient.getObject(anyString(), anyString())).thenReturn(obsObject);
        when(obsClient.putObject(anyString(), anyString(), any(ByteArrayInputStream.class), any(ObjectMetadata.class))).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("file",
                "test.jpg",
                "image/jpeg",
                "test".getBytes(StandardCharsets.UTF_8)
        );
        assertThrows(AgentStudioException.class,
            () -> obsService.upLoadImage("mock", "mock", file));
    }

    // ========== getObsTempUrl ==========

    @Test
    void testGetObsTempUrl_Exception_Throws() {
        when(obsClient.createTemporarySignature(any(com.obs.services.model.TemporarySignatureRequest.class)))
            .thenThrow(new RuntimeException("OBS error"));

        assertThrows(AgentStudioException.class,
            () -> obsService.getObsTempUrl("image/file-001", 3600L));
    }

    // ========== uploadObsFile (inputStream, objectKey) ==========

    @Test
    void testUploadObsFile_Success() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8));
        when(obsClient.putObject(anyString(), anyString(), any(ByteArrayInputStream.class), any())).thenReturn(null);

        obsService.uploadObsFile(inputStream, "test/path.txt");
        verify(obsClient).putObject(anyString(), anyString(), any(ByteArrayInputStream.class), any());
    }

    @Test
    void testUploadObsFile_ObsException_Throws() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
        when(obsClient.putObject(anyString(), anyString(), any(ByteArrayInputStream.class), any()))
            .thenThrow(ObsException.class);

        assertThrows(AgentStudioException.class,
            () -> obsService.uploadObsFile(inputStream, "test/path.txt"));
    }

    // ========== downloadObsFile ==========

    @Test
    void testDownloadObsFile_Success() throws IOException {
        String content = "downloaded content";
        ObsObject obsObj = new ObsObject();
        ObjectMetadata metadata = new ObjectMetadata();
        obsObj.setMetadata(metadata);
        obsObj.setObjectContent(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

        when(obsClient.getObject("test-bucket", "path/file.txt")).thenReturn(obsObj);

        String result = obsService.downloadObsFile("path/file.txt");
        assertNotNull(result);
    }

    @Test
    void testDownloadObsFile_ObsException_Throws() {
        when(obsClient.getObject("test-bucket", "nonexistent.txt")).thenThrow(ObsException.class);

        assertThrows(AgentStudioException.class,
            () -> obsService.downloadObsFile("nonexistent.txt"));
    }

    // ========== deleteObsFile ==========

    @Test
    void testDeleteObsFile_Exist() {
        when(obsClient.doesObjectExist("test-bucket", "path/file.txt")).thenReturn(true);

        obsService.deleteObsFile("path/file.txt");
        verify(obsClient).deleteObject("test-bucket", "path/file.txt");
    }

    @Test
    void testDeleteObsFile_NotExist() {
        when(obsClient.doesObjectExist("test-bucket", "path/file.txt")).thenReturn(false);

        obsService.deleteObsFile("path/file.txt");
        verify(obsClient, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void testDeleteObsFile_ObsException_Throws() {
        when(obsClient.doesObjectExist("test-bucket", "path/file.txt")).thenThrow(ObsException.class);

        assertThrows(AgentStudioException.class,
            () -> obsService.deleteObsFile("path/file.txt"));
    }

    // ========== isExistObsFile ==========

    @Test
    void testIsExistObsFile_Exists() {
        when(obsClient.doesObjectExist("test-bucket", "path/file.txt")).thenReturn(true);

        assertTrue(obsService.isExistObsFile("path/file.txt"));
    }

    @Test
    void testIsExistObsFile_NotExists() {
        when(obsClient.doesObjectExist("test-bucket", "path/file.txt")).thenReturn(false);

        assertFalse(obsService.isExistObsFile("path/file.txt"));
    }

    @Test
    void testIsExistObsFile_Exception() {
        when(obsClient.doesObjectExist("test-bucket", "path/file.txt")).thenThrow(new RuntimeException("error"));

        assertFalse(obsService.isExistObsFile("path/file.txt"));
    }

    // ========== copyFile ==========

    @Test
    void testCopyFile_Success() {
        obsService.copyFile("old/path.txt", "new/path.txt");
        verify(obsClient).copyObject("test-bucket", "old/path.txt", "test-bucket", "new/path.txt");
    }

    @Test
    void testCopyFile_NullOldPath() {
        obsService.copyFile(null, "new/path.txt");
        verify(obsClient, never()).copyObject(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testCopyFile_NullNewPath() {
        obsService.copyFile("old/path.txt", null);
        verify(obsClient, never()).copyObject(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testCopyFile_EmptyPaths() {
        obsService.copyFile("", "");
        verify(obsClient, never()).copyObject(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testCopyFile_Exception_Throws() {
        doThrow(new RuntimeException("copy failed")).when(obsClient)
            .copyObject(anyString(), anyString(), anyString(), anyString());

        assertThrows(AgentStudioException.class,
            () -> obsService.copyFile("old/path.txt", "new/path.txt"));
    }

    // ========== listObjectKeys ==========

    @Test
    void testListObjectKeys_Success() {
        ObjectListing listing = mock(ObjectListing.class);
        ObsObject obj1 = new ObsObject();
        obj1.setObjectKey("dir/file1.txt");
        ObsObject obj2 = new ObsObject();
        obj2.setObjectKey("dir/file2.txt");
        when(listing.getObjects()).thenReturn(List.of(obj1, obj2));

        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(listing);

        List<String> keys = obsService.listObjectKeys("dir");
        assertEquals(2, keys.size());
        assertEquals("dir/file1.txt", keys.get(0));
    }

    @Test
    void testListObjectKeys_NullObjects() {
        ObjectListing listing = mock(ObjectListing.class);
        when(listing.getObjects()).thenReturn(Collections.emptyList());

        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(listing);

        List<String> keys = obsService.listObjectKeys("dir");
        assertTrue(keys.isEmpty());
    }
}
