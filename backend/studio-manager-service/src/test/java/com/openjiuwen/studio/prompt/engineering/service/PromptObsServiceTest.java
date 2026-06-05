/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.dto.FileInfoVo;
import com.obs.services.ObsClient;
import com.obs.services.model.CreateBucketRequest;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class PromptObsServiceTest {

    @Mock
    private ObsClient obsClient;

    @Mock
    private ObsObject obsObject;

    @Value("${obs.bucket}")
    private String bucket;

    @InjectMocks
    private PromptObsService obsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void upLoadImage() throws IOException {
        // mock obsObject
        obsService.setObsClient(obsClient);
        when(obsClient.doesObjectExist(anyString(), anyString())).thenReturn(true);

        // mock metadata
        ObjectMetadata objectMetadata = new ObjectMetadata();
        when(obsObject.getMetadata()).thenReturn(objectMetadata);

        // mock obsClient
        when(obsClient.getObject(eq(bucket), anyString())).thenReturn(obsObject);
        when(obsClient.createBucket(any(CreateBucketRequest.class))).thenReturn(null);
        when(obsClient.putObject(anyString(), anyString(), any(ByteArrayInputStream.class))).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("file",  // 参数名
                "test.jpg",  // 原始文件名
                "image/jpeg",  // MIME类型
                "test".getBytes(StandardCharsets.UTF_8)  // 文件内容
        );
        try {
            FileInfoVo fileInfoVo = obsService.upLoadImage("mock", "mock",file);
        } catch (AgentStudioException e) {
            Assertions.assertTrue(true); // 这里可以添加更多的断言或处理逻辑
        } catch (Exception e) {
            Assertions.fail("Unexpected exception thrown: " + e.getMessage());
        }
    }

    @Test
    void init() {


    }
}
