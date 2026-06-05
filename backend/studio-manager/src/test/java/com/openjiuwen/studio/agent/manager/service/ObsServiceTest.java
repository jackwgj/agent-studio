/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_PROJECT_ID;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.CreateBucketRequest;
import com.obs.services.model.DeleteObjectsRequest;
import com.obs.services.model.DeleteObjectsResult;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;
import com.obs.services.model.TemporarySignatureRequest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * obs测试
 *
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class ObsServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObsClient obsClient;

    @InjectMocks
    private MgObsService obsService;

    private AutoCloseable mockitoCloseable;

    @BeforeAll
    static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
    }

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(obsService, "obsClient", obsClient);
        ReflectionTestUtils.setField(obsService, "bucket", "workflow-ir");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testObsUploadFile() {
        String objectKey = Constants.TEST_DIR_NAME + "/" + Constants.TEST_WORKFLOW_ID + ".json";
        Map<String, Object> irInfo = new HashMap<>();
        irInfo.put("components", new ArrayList<>());
        when(obsClient.headBucket(eq(Constants.TEST_BUCKET_NAME))).thenReturn(false);
        when(obsClient.createBucket(any(CreateBucketRequest.class))).thenReturn(null);
        when(obsClient.putObject(eq(Constants.TEST_BUCKET_NAME), eq(objectKey), any(ByteArrayInputStream.class)))
            .thenReturn(null);
        obsService.uploadObsFile(Constants.TEST_WORKFLOW_ID, Constants.TEST_WORKFLOW_ID, CommonConstant.WORKFLOW,
            JSON.toJSONString(irInfo), CommonConstant.Workflow.FLOW);
    }

    @Test
    void testObsUploadFailed() {
        Map<String, Object> irInfo = new HashMap<>();
        irInfo.put("components", new ArrayList<>());
        when(obsClient.headBucket(eq(Constants.TEST_BUCKET_NAME))).thenReturn(false);
        when(obsClient.createBucket(any(CreateBucketRequest.class))).thenThrow(ObsException.class);
        Assertions.assertThrows(AgentStudioException.class, () -> {
            obsService.uploadObsFile(Constants.TEST_WORKFLOW_ID, Constants.TEST_WORKFLOW_ID, CommonConstant.WORKFLOW,
                JSON.toJSONString(irInfo), CommonConstant.Workflow.FLOW);
        });
    }

    @Test
    void testObsDownloadFile() {
        String objectKey = Constants.TEST_DIR_NAME + "/" + Constants.TEST_WORKFLOW_ID + ".json";
        when(obsClient.getObject(eq(Constants.TEST_BUCKET_NAME), eq(objectKey))).thenThrow(ObsException.class);
        Assertions.assertThrows(AgentStudioException.class, () -> {
            obsService.downloadObsFile(objectKey);
        });
    }

    @Test
    void testObsDeleteFile() {
        String objectKey = Constants.TEST_DIR_NAME + "/" + Constants.TEST_WORKFLOW_ID + ".json";
        when(obsClient.doesObjectExist(eq(Constants.TEST_BUCKET_NAME), eq(objectKey))).thenReturn(true);
        when(obsClient.deleteObject(eq(Constants.TEST_BUCKET_NAME), eq(objectKey))).thenReturn(null);
        obsService.deleteObsFile(objectKey);
    }

    @Test
    void testObsDeleteFileFailed() {
        String objectKey = Constants.TEST_DIR_NAME + "/" + Constants.TEST_WORKFLOW_ID + ".json";
        when(obsClient.doesObjectExist(eq(Constants.TEST_BUCKET_NAME), eq(objectKey))).thenReturn(true);
        when(obsClient.deleteObject(eq(Constants.TEST_BUCKET_NAME), eq(objectKey))).thenThrow(ObsException.class);
        Assertions.assertThrows(AgentStudioException.class, () -> obsService.deleteObsFile(objectKey));
    }

    @Test
    void testObsDeleteObjects() {
        String dirPath = Constants.TEST_DIR_NAME + "/" + Constants.TEST_WORKFLOW_ID;
        ListObjectsRequest request = new ListObjectsRequest();
        ObjectListing objects = new ObjectListing.Builder().objectSummaries(new ArrayList<>()).builder();
        when(obsClient.listObjects(request)).thenReturn(objects);
        obsService.deleteObsObjects(dirPath);
    }

    @Test
    void testUploadObsFileWithExpires() throws IOException {
        when(obsClient.headBucket(eq(Constants.TEST_BUCKET_NAME))).thenReturn(false);
        when(obsClient.createBucket(any(CreateBucketRequest.class))).thenReturn(null);
        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        obsService.uploadObsFileWithExpires(mockInputStream(), "name", 100);
    }

    @Test
    void testCleanObsObjects() {
        ListObjectsRequest request = new ListObjectsRequest();
        ObjectListing objects = new ObjectListing.Builder().objectSummaries(new ArrayList<>()).builder();
        when(obsClient.listObjects(request)).thenReturn(objects);
        obsService.cleanObsObjects();
    }

    @Test
    void testGetTemporaryGetRsp() {
        when(obsClient.createTemporarySignature(any(TemporarySignatureRequest.class))).thenReturn(null);
        assertNull(obsService.getTemporaryGetRsp(true, "obsKey", 100));
    }

    private InputStream mockInputStream() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file",  // 参数名
            "test.txt",  // 原始文件名
            "text/plain",  // MIME类型
            "test".getBytes(StandardCharsets.UTF_8)  // 文件内容
        );
        return file.getInputStream();
    }

    @Test
    public void testDeleteByPrefix() {
        ObjectListing objectListing = mock(ObjectListing.class);
        List<ObsObject> obsObjects = new ArrayList<>();
        ObsObject obsObject = new ObsObject();
        obsObject.setObjectKey("mock_obs_key");
        obsObjects.add(obsObject);
        when(objectListing.getObjects()).thenReturn(obsObjects);
        when(obsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);
        when(obsClient.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(new DeleteObjectsResult());
        Assertions.assertNotNull(obsService.deleteByPrefix("mock_delete_obs_prefix"));
    }
}
