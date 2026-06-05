/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;
import com.obs.services.ObsClient;
import com.obs.services.model.CreateBucketRequest;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;

import okhttp3.Call;
import okhttp3.OkHttpClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * obs服务单元测试
 *
 */
public class ObsServiceTest extends BaseTest {
    private static final String OBS_CONTENT = "obs_content";

    private static final String OBS_MD5 = "OBS_MD5";

    @MockitoBean
    RedissonClient redissonClientMock;

    @Mock
    private ObsClient obsClient;

    @Mock
    private ObsObject obsObject;

    @Mock
    private OkHttpUtils okHttpUtils;

    @Value("${obs.bucket}")
    private String bucket;

    @Autowired
    private ObsService obsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(obsService, "okHttpUtils", okHttpUtils);
    }

    @Test
    public void testPutObject() {
        obsService.setObsClient(obsClient);
        when(obsClient.headBucket(eq(bucket))).thenReturn(false);
        when(obsClient.createBucket(any(CreateBucketRequest.class))).thenReturn(null);
        when(obsClient.putObject(anyString(), anyString(), any(ByteArrayInputStream.class))).thenReturn(null);
        obsService.putObject("", "", "");

        when(obsClient.putObject(any(PutObjectRequest.class))).thenReturn(null);
        obsService.putObject("", new ByteArrayInputStream(OBS_CONTENT.getBytes(StandardCharsets.UTF_8)), 1);
    }

    @Test
    public void testGetObject() {
        // mock obsObject
        obsService.setObsClient(obsClient);
        when(obsClient.doesObjectExist(anyString(), anyString())).thenReturn(true);

        when(obsObject.getObjectContent()).thenReturn(
            new ByteArrayInputStream(OBS_CONTENT.getBytes(StandardCharsets.UTF_8)));

        // mock metadata
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentMd5(OBS_MD5);
        when(obsObject.getMetadata()).thenReturn(objectMetadata);

        // mock obsClient
        when(obsClient.getObject(eq(bucket), anyString())).thenReturn(obsObject);
        when(obsClient.createBucket(any(CreateBucketRequest.class))).thenReturn(null);
        when(obsClient.putObject(anyString(), anyString(), any(ByteArrayInputStream.class))).thenReturn(null);
        Map<String, String> result = obsService.getObject("", "");
        assertEquals(OBS_CONTENT, result.get(Constant.Obs.CONTENT));
        assertEquals(OBS_MD5, result.get(Constant.Obs.MD5));
    }

    @Test
    public void testGetMd5() {
        // mock metadata
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentMd5(OBS_MD5);
        when(obsObject.getMetadata()).thenReturn(objectMetadata);

        // mock obsClient
        obsService.setObsClient(obsClient);
        when(obsClient.getObject(eq(bucket), anyString())).thenReturn(obsObject);
        assertEquals(OBS_MD5, obsService.getMd5("", ""));
    }

    @Test
    public void testGetByUrl() throws IOException {
        OkHttpClient okHttpClient = mock(OkHttpClient.class);
        when(okHttpUtils.getHttpClient()).thenReturn(okHttpClient);
        Call mockCall = mock(Call.class);
        when(okHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(SocketTimeoutException.class);
        String url = "http://test.com/test.txt?";
        assertThrows(AgentStudioException.class, () -> obsService.getByUrl(url));
    }

    @Test
    public void testUploadObsFileWithExpires() throws IOException {
        // mock obsObject
        obsService.setObsClient(obsClient);
        when(obsClient.doesObjectExist(anyString(), anyString())).thenReturn(true);

        when(obsObject.getObjectContent()).thenReturn(
            new ByteArrayInputStream(OBS_CONTENT.getBytes(StandardCharsets.UTF_8)));

        // mock metadata
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentMd5(OBS_MD5);
        when(obsObject.getMetadata()).thenReturn(objectMetadata);

        // mock obsClient
        when(obsClient.getObject(eq(bucket), anyString())).thenReturn(obsObject);
        when(obsClient.createBucket(any(CreateBucketRequest.class))).thenReturn(null);
        when(obsClient.putObject(anyString(), anyString(), any(ByteArrayInputStream.class))).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("file",  // 参数名
            "test.txt",  // 原始文件名
            "text/plain",  // MIME类型
            "test".getBytes(StandardCharsets.UTF_8)  // 文件内容
        );
        InputStream inputStream = file.getInputStream();
        String url = obsService.uploadObsFileWithExpires(inputStream, "test.txt", 300);
        assertNotNull(url);
    }

    @Test
    public void uploadWithPublicReadTest() {
        String file;
        try {
            file = obsService.uploadToStagingWithPublicRead(
                new ByteArrayInputStream(OBS_CONTENT.getBytes(StandardCharsets.UTF_8)), "demo", 10);
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.OBS_FAILED, e.getErrorCode());
        }

        // mock obsObject
        obsService.setObsClient(obsClient);
        when(obsClient.doesObjectExist(anyString(), anyString())).thenReturn(true);

        when(obsObject.getObjectContent()).thenReturn(
            new ByteArrayInputStream(OBS_CONTENT.getBytes(StandardCharsets.UTF_8)));

        // mock metadata
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentMd5(OBS_MD5);
        when(obsObject.getMetadata()).thenReturn(objectMetadata);

        // mock obsClient
        when(obsClient.getObject(eq(bucket), anyString())).thenReturn(obsObject);
        when(obsClient.createBucket(any(CreateBucketRequest.class))).thenReturn(null);
        when(obsClient.putObject(anyString(), anyString(), any(ByteArrayInputStream.class))).thenReturn(null);

        InputStream inputStream = new ByteArrayInputStream(OBS_CONTENT.getBytes(StandardCharsets.UTF_8));
        String url = obsService.uploadToStagingWithPublicRead(inputStream, "test.txt", 300);
        Assertions.assertEquals("https://www.fake.com/agentBuilder-files-staging/file/test.txt", url);
    }
}
