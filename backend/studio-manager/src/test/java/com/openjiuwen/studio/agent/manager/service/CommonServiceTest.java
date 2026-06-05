/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_TAG_ID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.common.dto.knowledge.FileUploadRsp;
import com.openjiuwen.studio.agent.manager.dto.TagInfo;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
public class CommonServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    @Autowired
    CommonManagementService commonManagementService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService mgObsService;

    private AutoCloseable mockitoCloseable;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId("mock", TEST_PROJECT_ID);
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_USER_ID);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
    }

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(commonManagementService, "mgObsService", mgObsService);
        ReflectionTestUtils.setField(commonManagementService, "envType", "simple");
        ReflectionTestUtils.setField(commonManagementService, "knowledgeSource", "LakeSearch");
        ReflectionTestUtils.setField(commonManagementService, "frontPageBlockNodes",
            "DataAcquisition,DataProcess,Http,Sql");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testListTags() {
        List<TagInfo> tagInfoList = commonManagementService.listTags(TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID);

        assertNotNull(tagInfoList);
        Set<String> tagIds = tagInfoList.stream().map(TagInfo::getTagId).collect(Collectors.toSet());
        assertFalse(tagIds.contains(TEST_TAG_ID));
    }


    @Test
    void testUploadIconErrorType() {
        assertThrows(AgentStudioException.class, () -> {
            // set up
            MultipartFile file = new MockMultipartFile("cat", "ca..//t.png", "image/png",
                "hello".getBytes(StandardCharsets.UTF_8));

            // run test
            commonManagementService.uploadAvatar(TEST_PROJECT_ID, file);
        });
    }


    @Test
    public void testSystemSettings() {
        ReflectionTestUtils.setField(commonManagementService, "knowledgeSource", "KooSearch");
        commonManagementService.init();
        commonManagementService.systemSettings(TEST_PROJECT_ID, "front");
    }

    @Test
    public void testSystemSettingsWhenHCS() {
        ReflectionTestUtils.setField(commonManagementService, "knowledgeSource", "KooSearch");
        ReflectionTestUtils.setField(commonManagementService, "envType", "hcs");
        Object o = commonManagementService.systemSettings(TEST_PROJECT_ID, "front");
    }

    @Test
    void test_uploadAvatar_should_succeed_when_test_data_combination() {
        // set up
        MultipartFile file = new MockMultipartFile("cat", "cat.png", "image/png",
            "hello".getBytes(StandardCharsets.UTF_8));
        when(mgObsService.uploadObsFile(anyString(), any(InputStream.class), anyInt())).thenReturn("fake_object_key");

        // run test
        FileUploadRsp fileUploadRsp = commonManagementService.uploadAvatar(TEST_PROJECT_ID, file);

        // assert result
        assertTrue(!Objects.isNull(fileUploadRsp) && StringUtils.isNotBlank(fileUploadRsp.getFileName())
            && fileUploadRsp.getFileName().endsWith(".png"));
    }

    @Test
    void test_uploadAvatar_should_failed_when_file_name_is_illegal() {
        // assert result
        assertThrows(AgentStudioException.class, () -> {
            // set up
            MultipartFile file = new MockMultipartFile("cat", "ca..//t.png", "image/png",
                "hello".getBytes(StandardCharsets.UTF_8));

            // run test
            commonManagementService.uploadAvatar(TEST_PROJECT_ID, file);
        });
    }

    @Test
    void test_uploadAvatar_should_failed_when_file_type_is_illegal() {
        // assert result
        assertThrows(AgentStudioException.class, () -> {
            // set up
            MultipartFile file = new MockMultipartFile("cat", "cat.pdf", "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8));

            // run test
            commonManagementService.uploadAvatar(TEST_PROJECT_ID, file);
        });
    }

    @Test
    void test_uploadAvatar_should_failed_when_file_is_null() {
        // assert result
        assertThrows(AgentStudioException.class, () -> {
            // run test
            commonManagementService.uploadAvatar(TEST_PROJECT_ID, null);
        });
    }
}
