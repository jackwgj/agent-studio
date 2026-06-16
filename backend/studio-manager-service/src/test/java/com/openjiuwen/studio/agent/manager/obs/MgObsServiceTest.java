/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.obs;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.BucketVersioningConfiguration;
import com.obs.services.model.CreateBucketRequest;
import com.obs.services.model.DeleteObjectsRequest;
import com.obs.services.model.DeleteObjectsResult;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.obs.services.model.VersioningStatusEnum;
import com.obs.services.model.fs.NewFolderRequest;
import com.obs.services.model.fs.ObsFSFolder;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MgObsServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObsClient obsClient;

    @InjectMocks
    private MgObsService mgObsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mgObsService, "obsClient", obsClient);
        ReflectionTestUtils.setField(mgObsService, "opensource", true);
        ReflectionTestUtils.setField(mgObsService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(mgObsService, "stagingBucket", "test-staging-bucket");
    }

    @Test
    void testGetAbsolutePath() {
        String result = mgObsService.getAbsolutePath("path/to/file");
        assertEquals("obs://test-bucket/path/to/file", result);
    }

    @Test
    void testIsExistedKey_True() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenReturn(true);
        assertTrue(mgObsService.isExistedKey("key"));
    }

    @Test
    void testIsExistedKey_False() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenReturn(false);
        assertFalse(mgObsService.isExistedKey("key"));
    }

    @Test
    void testDeleteObsFile_Exists() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenReturn(true);
        mgObsService.deleteObsFile("key");
        verify(obsClient).deleteObject("test-bucket", "key");
    }

    @Test
    void testDeleteObsFile_NotExists() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenReturn(false);
        mgObsService.deleteObsFile("key");
        verify(obsClient, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void testDeleteObsFile_ObsException() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.deleteObsFile("key"));
        assertEquals(StudioError.OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testSoftDeleteObsFile_Exists() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenReturn(true);
        mgObsService.softDeleteObsFile("key");
        verify(obsClient).copyObject("test-bucket", "key", "test-bucket", "key.deleted");
        verify(obsClient).deleteObject("test-bucket", "key");
    }

    @Test
    void testSoftDeleteObsFile_NotExists() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenReturn(false);
        mgObsService.softDeleteObsFile("key");
        verify(obsClient, never()).copyObject(anyString(), anyString(), anyString(), anyString());
        verify(obsClient, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void testSoftDeleteObsFile_ObsException() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.softDeleteObsFile("key"));
        assertEquals(StudioError.OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testCopyObsObject_Success() {
        mgObsService.copyObsObject("source", "target");
        verify(obsClient).copyObject("test-bucket", "source", "test-bucket", "target");
    }

    @Test
    void testCopyObsObject_ObsException() {
        when(obsClient.copyObject(anyString(), anyString(), anyString(), anyString())).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.copyObsObject("source", "target"));
        assertEquals(StudioError.COPY_FROM_OBS_FAIL, ex.getErrorCode());
    }

    @Test
    void testGetObjectMetadata_Exists() {
        ObjectMetadata metadata = new ObjectMetadata();
        ObsObject mockObject = mock(ObsObject.class);
        when(mockObject.getMetadata()).thenReturn(metadata);
        when(obsClient.doesObjectExist("test-bucket", "key")).thenReturn(true);
        when(obsClient.getObject("test-bucket", "key")).thenReturn(mockObject);
        ObjectMetadata result = mgObsService.getObjectMetadata("key");
        assertNotNull(result);
        assertEquals(metadata, result);
    }

    @Test
    void testGetObjectMetadata_NotExists() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenReturn(false);
        ObjectMetadata result = mgObsService.getObjectMetadata("key");
        assertNotNull(result);
    }

    @Test
    void testGetObjectMetadata_ObsException() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenThrow(new ObsException("error"));
        ObjectMetadata result = mgObsService.getObjectMetadata("key");
        assertNotNull(result);
    }

    @Test
    void testGetTemporaryGetRsp_MainBucket() {
        TemporarySignatureResponse mockResponse = mock(TemporarySignatureResponse.class);
        when(obsClient.createTemporarySignature(any(TemporarySignatureRequest.class))).thenReturn(mockResponse);
        TemporarySignatureResponse result = mgObsService.getTemporaryGetRsp(false, "object", 3600);
        assertEquals(mockResponse, result);
    }

    @Test
    void testGetTemporaryGetRsp_StagingBucket() {
        TemporarySignatureResponse mockResponse = mock(TemporarySignatureResponse.class);
        when(obsClient.createTemporarySignature(any(TemporarySignatureRequest.class))).thenReturn(mockResponse);
        TemporarySignatureResponse result = mgObsService.getTemporaryGetRsp(true, "object", 3600);
        assertEquals(mockResponse, result);
    }

    @Test
    void testGetTemporaryGetRsp_ObsException() {
        when(obsClient.createTemporarySignature(any(TemporarySignatureRequest.class))).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.getTemporaryGetRsp(false, "object", 3600));
        assertEquals(StudioError.GET_OBS_TEMPORARY_URL_FAILED, ex.getErrorCode());
    }

    @Test
    void testNewFolder_Success() {
        NewFolderRequest request = mock(NewFolderRequest.class);
        ObsFSFolder mockFolder = mock(ObsFSFolder.class);
        when(obsClient.newFolder(request)).thenReturn(mockFolder);
        ObsFSFolder result = mgObsService.newFolder(request);
        assertEquals(mockFolder, result);
    }

    @Test
    void testNewFolder_ObsException() {
        NewFolderRequest request = mock(NewFolderRequest.class);
        when(obsClient.newFolder(request)).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.newFolder(request));
        assertEquals(StudioError.OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testUploadObsFile_StringContent_Success() {
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        String result = mgObsService.uploadObsFile("key", "content", -1);
        assertEquals("key", result);
    }

    @Test
    void testUploadObsFile_StringContent_Error() {
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.uploadObsFile("key", "content", -1));
        assertEquals(StudioError.UPLOAD_FILE_TO_OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testUploadObsFile_Stream_Success() {
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        InputStream stream = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        String result = mgObsService.uploadObsFile("key", stream, -1);
        assertEquals("key", result);
    }

    @Test
    void testUploadObsFile_Stream_WithExpires() {
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        InputStream stream = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        String result = mgObsService.uploadObsFile("key", stream, 7);
        assertEquals("key", result);
    }

    @Test
    void testUploadObsFile_Stream_Error() {
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenThrow(new ObsException("error"));
        InputStream stream = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.uploadObsFile("key", stream, -1));
        assertEquals(StudioError.UPLOAD_FILE_TO_OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testUploadObsFile_Stream_BucketCreation() {
        when(obsClient.headBucket("test-bucket")).thenReturn(false);
        when(obsClient.createBucket(any(CreateBucketRequest.class))).thenReturn(null);
        when(obsClient.setBucketVersioning(eq("test-bucket"), any(BucketVersioningConfiguration.class))).thenReturn(null);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        InputStream stream = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        String result = mgObsService.uploadObsFile("key", stream, -1);
        assertEquals("key", result);
        verify(obsClient).createBucket(any(CreateBucketRequest.class));
        verify(obsClient).setBucketVersioning(eq("test-bucket"), any(BucketVersioningConfiguration.class));
    }

    @Test
    void testUploadObsFile_Stream_BucketCreationFails() {
        when(obsClient.headBucket("test-bucket")).thenReturn(false);
        when(obsClient.createBucket(any(CreateBucketRequest.class))).thenThrow(new ObsException("error"));
        InputStream stream = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.uploadObsFile("key", stream, -1));
        assertEquals(StudioError.OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testUploadObsFile_NotOpensource() {
        ReflectionTestUtils.setField(mgObsService, "opensource", false);
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        InputStream stream = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        String result = mgObsService.uploadObsFile("key", stream, -1);
        assertEquals("key", result);
    }

    @Test
    void testUploadStagingBucket_Success() {
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        String result = mgObsService.uploadStagingBucket("key", "content", -1);
        assertEquals("key", result);
    }

    @Test
    void testUploadStreamStagingBucket_Success() {
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        InputStream stream = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        String result = mgObsService.uploadStreamStagingBucket("key", stream, -1);
        assertEquals("key", result);
    }

    @Test
    void testUploadObsFile_5Param_Success() {
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        String result = mgObsService.uploadObsFile("pathKey", "objectKey", "type", "fileInfo", "prefix");
        assertEquals("type/prefix/pathKey/objectKey.json", result);
    }

    @Test
    void testUploadObsFileWithExpires_Success() {
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        InputStream stream = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        String result = mgObsService.uploadObsFileWithExpires(stream, "file.json", 7);
        assertEquals("file/file.json", result);
    }

    @Test
    void testDownloadObsFile_Success() {
        ObsObject mockObject = mock(ObsObject.class);
        InputStream contentStream = new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8));
        when(mockObject.getObjectContent()).thenReturn(contentStream);
        when(obsClient.getObject("test-bucket", "key")).thenReturn(mockObject);
        String result = mgObsService.downloadObsFile("key");
        assertEquals("test content", result);
    }

    @Test
    void testDownloadObsFile_Exception() {
        when(obsClient.getObject("test-bucket", "key")).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.downloadObsFile("key"));
        assertEquals(StudioError.OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testDownloadObsImageFile_Success() {
        ObsObject mockObject = mock(ObsObject.class);
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};
        InputStream contentStream = new ByteArrayInputStream(imageBytes);
        when(mockObject.getObjectContent()).thenReturn(contentStream);
        when(obsClient.getObject("test-bucket", "icon/key")).thenReturn(mockObject);
        String result = mgObsService.downloadObsImageFile("key");
        assertEquals(java.util.Base64.getEncoder().encodeToString(imageBytes), result);
    }

    @Test
    void testDownloadObsImageFile_NullContent() {
        ObsObject mockObject = mock(ObsObject.class);
        when(mockObject.getObjectContent()).thenReturn(null);
        when(obsClient.getObject("test-bucket", "icon/key")).thenReturn(mockObject);
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.downloadObsImageFile("key"));
        assertEquals(StudioError.OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testDownloadObsImageFile_Exception() {
        when(obsClient.getObject("test-bucket", "icon/key")).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.downloadObsImageFile("key"));
        assertEquals(StudioError.OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testListObsObjects_WithResults() {
        ObsObject mockObject = mock(ObsObject.class);
        ObjectListing mockListing = mock(ObjectListing.class);
        when(mockListing.getObjects()).thenReturn(List.of(mockObject));
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(mockListing);
        List<ObsObject> result = mgObsService.listObsObjects("path");
        assertEquals(1, result.size());
    }

    @Test
    void testListObsObjects_EmptyListing() {
        ObjectListing mockListing = mock(ObjectListing.class);
        when(mockListing.getObjects()).thenReturn(List.of());
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(mockListing);
        List<ObsObject> result = mgObsService.listObsObjects("path");
        assertTrue(result.isEmpty());
    }

    @Test
    void testListPageObsObjects_Success() {
        ObsObject mockObject = mock(ObsObject.class);
        ObjectListing mockListing = mock(ObjectListing.class);
        when(mockListing.getObjects()).thenReturn(List.of(mockObject));
        when(mockListing.isTruncated()).thenReturn(false);
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(mockListing);
        List<ObsObject> result = mgObsService.listPageObsObjects("path", 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void testListPageObsObjects_ObsException() {
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.listPageObsObjects("path", 0, 10));
        assertEquals(StudioError.OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testDeleteObsObjects_Success() {
        ObsObject mockObject = mock(ObsObject.class);
        when(mockObject.getObjectKey()).thenReturn("dir/file1.json");
        ObjectListing mockListing = mock(ObjectListing.class);
        when(mockListing.getObjects()).thenReturn(List.of(mockObject));
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(mockListing);
        mgObsService.deleteObsObjects("dir");
        verify(obsClient).deleteObject("test-bucket", "dir/file1.json");
    }

    @Test
    void testDeleteObsObjects_ObsException() {
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.deleteObsObjects("dir"));
        assertEquals(StudioError.OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testCleanObsObjects_NotOpensource() {
        ReflectionTestUtils.setField(mgObsService, "opensource", false);
        mgObsService.cleanObsObjects();
        verify(obsClient, never()).listObjects(any(ListObjectsRequest.class));
    }

    @Test
    void testCleanObsObjects_Opensource_NoObjects() {
        ObjectListing mockListing = mock(ObjectListing.class);
        when(mockListing.getObjects()).thenReturn(List.of());
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(mockListing);
        mgObsService.cleanObsObjects();
        verify(obsClient, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void testCleanObsObjects_Opensource_WithOldFiles() {
        ObsObject mockObject = mock(ObsObject.class);
        ObjectMetadata mockMetadata = mock(ObjectMetadata.class);
        when(mockObject.getMetadata()).thenReturn(mockMetadata);
        when(mockMetadata.getLastModified()).thenReturn(Date.from(Instant.now().minus(Duration.ofDays(8))));
        when(mockObject.getObjectKey()).thenReturn("file/old-file.json");

        ObjectListing mockListing = mock(ObjectListing.class);
        when(mockListing.getObjects()).thenReturn(List.of(mockObject));
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(mockListing);
        when(obsClient.doesObjectExist("test-bucket", "file/old-file.json")).thenReturn(true);

        mgObsService.cleanObsObjects();
        verify(obsClient).deleteObject("test-bucket", "file/old-file.json");
    }

    @Test
    void testCleanObsObjects_Opensource_WithRecentFiles() {
        ObsObject mockObject = mock(ObsObject.class);
        ObjectMetadata mockMetadata = mock(ObjectMetadata.class);
        when(mockObject.getMetadata()).thenReturn(mockMetadata);
        when(mockMetadata.getLastModified()).thenReturn(Date.from(Instant.now().minus(Duration.ofDays(1))));
        when(mockObject.getObjectKey()).thenReturn("file/recent-file.json");

        ObjectListing mockListing = mock(ObjectListing.class);
        when(mockListing.getObjects()).thenReturn(List.of(mockObject));
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(mockListing);

        mgObsService.cleanObsObjects();
        verify(obsClient, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void testCleanObsObjects_ObsException() {
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenThrow(new ObsException("error"));
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> mgObsService.cleanObsObjects());
        assertEquals(StudioError.OBS_FAILED, ex.getErrorCode());
    }

    @Test
    void testPutObject_Success() {
        when(obsClient.headBucket("test-bucket")).thenReturn(true);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        mgObsService.putObject("key", "content");
        verify(obsClient).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testDeleteObject_Exists() {
        when(obsClient.doesObjectExist("test-bucket", "key")).thenReturn(true);
        mgObsService.deleteObject("key");
        verify(obsClient).deleteObject("test-bucket", "key");
    }

    @Test
    void testGetObject_Success() {
        ObsObject mockObject = mock(ObsObject.class);
        InputStream contentStream = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        when(mockObject.getObjectContent()).thenReturn(contentStream);
        when(obsClient.getObject("test-bucket", "key")).thenReturn(mockObject);
        String result = mgObsService.getObject("key");
        assertEquals("content", result);
    }

    @Test
    void testDeleteByPrefix_NoObjects() {
        ObjectListing mockListing = mock(ObjectListing.class);
        when(mockListing.getObjects()).thenReturn(List.of());
        when(mockListing.isTruncated()).thenReturn(false);
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(mockListing);
        DeleteObjectsResult result = mgObsService.deleteByPrefix("prefix");
        assertNotNull(result);
        assertTrue(result.getDeletedObjectResults().isEmpty());
    }
}
