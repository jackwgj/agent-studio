/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.common.dto.knowledge.FileUploadRsp;
import com.openjiuwen.studio.agent.manager.dto.TagInfo;
import com.openjiuwen.studio.agent.manager.entity.Tag;
import com.openjiuwen.studio.agent.manager.mapper.AppMapper;
import com.openjiuwen.studio.agent.manager.mapper.TagMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CommonManagementServiceTest {

    @Mock
    private TagMapper tagMapper;

    @Mock
    private MgObsService mgObsService;

    @Mock
    private I18nUtil i18nUtil;

    @Mock
    private AppMapper appMapper;

    private CommonManagementService commonManagementService;

    @BeforeEach
    void setUp() {
        commonManagementService = new CommonManagementService(tagMapper, mgObsService, i18nUtil, appMapper);
        ReflectionTestUtils.setField(commonManagementService, "obsExpires", 3600);
        ReflectionTestUtils.setField(commonManagementService, "fileMaxSize", 10240L);
        ReflectionTestUtils.setField(commonManagementService, "imageMaxSize", 5120L);
        ReflectionTestUtils.setField(commonManagementService, "iconMaxSize", 1024L);
        ReflectionTestUtils.setField(commonManagementService, "allowedDefaultTypeStr", ".pdf,.doc,.docx");
        ReflectionTestUtils.setField(commonManagementService, "allowedIconTypeStr", ".png,.jpg,.jpeg");
        ReflectionTestUtils.setField(commonManagementService, "allowedImgTypeStr", ".png,.jpg");
        ReflectionTestUtils.setField(commonManagementService, "agentMcpBoundLimit", 10);
        ReflectionTestUtils.setField(commonManagementService, "agentWorkflowBoundLimit", 10);
        ReflectionTestUtils.setField(commonManagementService, "opProjectId", "op-proj");
        ReflectionTestUtils.setField(commonManagementService, "agentKnowledgeBoundLimit", 10);
        ReflectionTestUtils.setField(commonManagementService, "agentToolBoundLimit", 10);
        ReflectionTestUtils.setField(commonManagementService, "publicModelEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "kosEmbeddingModel", "kos-embed");
        ReflectionTestUtils.setField(commonManagementService, "kosReRankModel", "kos-rerank");
        ReflectionTestUtils.setField(commonManagementService, "mrsEmbeddingModel", "mrs-embed");
        ReflectionTestUtils.setField(commonManagementService, "mrsReRankModel", "mrs-rerank");
        ReflectionTestUtils.setField(commonManagementService, "knowledgeShareEnabled", true);
        ReflectionTestUtils.setField(commonManagementService, "knowledgeSource", "KOOSEARCH");
        ReflectionTestUtils.setField(commonManagementService, "knowledgeInternalEnabled", false);
        ReflectionTestUtils.setField(commonManagementService, "isOcrEnable", true);
        ReflectionTestUtils.setField(commonManagementService, "knowledgeRecallThresholdMin", 0.0);
        ReflectionTestUtils.setField(commonManagementService, "knowledgeRecallThresholdMax", 1.0);
        ReflectionTestUtils.setField(commonManagementService, "frontPageBlockNodes", "");
        ReflectionTestUtils.setField(commonManagementService, "knowledgeFileType", ".pdf,.doc,.txt");
        ReflectionTestUtils.setField(commonManagementService, "isAudioSupportInteraction", false);
        ReflectionTestUtils.setField(commonManagementService, "audioChineseTimbre", "[\"zh-1\"]");
        ReflectionTestUtils.setField(commonManagementService, "audioEnglishTimbre", "[\"en-1\"]");
        ReflectionTestUtils.setField(commonManagementService, "envType", "HC");
        ReflectionTestUtils.setField(commonManagementService, "opsMenuDisplay", true);
        ReflectionTestUtils.setField(commonManagementService, "safetyBarrierDisplay", true);
        ReflectionTestUtils.setField(commonManagementService, "deployWhiteLists", "*");
        ReflectionTestUtils.setField(commonManagementService, "agentOpsNewSwitch", false);
        ReflectionTestUtils.setField(commonManagementService, "opsLabelAllDisplay", true);
        ReflectionTestUtils.setField(commonManagementService, "opsEvaluationDisplay", false);
        ReflectionTestUtils.setField(commonManagementService, "longTermMemoryEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "agentToolPriorityEnable", true);
        ReflectionTestUtils.setField(commonManagementService, "publishAgentBuilderEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "codeEnvOptions", "");
        ReflectionTestUtils.setField(commonManagementService, "codeDefEnv", "python3");
        ReflectionTestUtils.setField(commonManagementService, "hisModelProtocolEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "spaceServiceDisplay", false);
        ReflectionTestUtils.setField(commonManagementService, "apiKeyEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "maxIteration", 30);
        ReflectionTestUtils.setField(commonManagementService, "chatWorkflowAsyncEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "workflowAsyncEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "sandboxEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "pluginChoice", false);
        ReflectionTestUtils.setField(commonManagementService, "userProfileEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "promptContentLength", 4096);
        ReflectionTestUtils.setField(commonManagementService, "publishAppEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "assetAppFreeTrialQuotaLimit", 10);
        ReflectionTestUtils.setField(commonManagementService, "maasRegion", "cn-north-4");
        ReflectionTestUtils.setField(commonManagementService, "maasSquarePage", "page-1");
        ReflectionTestUtils.setField(commonManagementService, "nl2agentDefaultModelId", "184");
        ReflectionTestUtils.setField(commonManagementService, "demoAgentId", "");
        ReflectionTestUtils.setField(commonManagementService, "demoWorkflowId", "");
        ReflectionTestUtils.setField(commonManagementService, "agentMultiModelEnable", false);
        ReflectionTestUtils.setField(commonManagementService, "platformProviderId", "100");
        ReflectionTestUtils.setField(commonManagementService, "displayShowModelStatus", false);
        ReflectionTestUtils.setField(commonManagementService, "licenseDefaultDisplayTime", 180);
        ReflectionTestUtils.setField(commonManagementService, "HMACAuthEnabled", false);
        ReflectionTestUtils.setField(commonManagementService, "runtimeHost", "");
        ReflectionTestUtils.setField(commonManagementService, "regionId", "");
        ReflectionTestUtils.setField(commonManagementService, "userProfilePrompt", "");
        ReflectionTestUtils.setField(commonManagementService, "semanticMemoryPrompt", "");
        commonManagementService.init();
    }

    @Test
    void testSystemSettings_Success() {
        try (MockedStatic<SpringBeanUtils> springBean = mockStatic(SpringBeanUtils.class);
             MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {

            springBean.when(() -> SpringBeanUtils.getProperty("front-page.configs", "{}")).thenReturn("{}");
            springBean.when(() -> SpringBeanUtils.getProperty("front-page.default-configs", "{}")).thenReturn("{}");
            reqCtx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            Object result = commonManagementService.systemSettings("proj-1", "default");
            assertNotNull(result);
            assertTrue(result instanceof JSONObject);
        }
    }

    @Test
    void testSystemSettings_WithConfigs() {
        try (MockedStatic<SpringBeanUtils> springBean = mockStatic(SpringBeanUtils.class);
             MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {

            springBean.when(() -> SpringBeanUtils.getProperty("front-page.configs", "{}"))
                .thenReturn("{\"key1\":\"value1\"}");
            springBean.when(() -> SpringBeanUtils.getProperty("front-page.default-configs", "{}"))
                .thenReturn("{\"key2\":\"value2\"}");
            reqCtx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            Object result = commonManagementService.systemSettings("proj-1", "default");
            assertNotNull(result);
            JSONObject json = (JSONObject) result;
            assertEquals("value1", json.getString("key1"));
            assertEquals("value2", json.getString("key2"));
        }
    }

    @Test
    void testUploadAvatar_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "test-image".getBytes());

        FileUploadRsp result = commonManagementService.uploadAvatar("proj-1", file);
        assertNotNull(result);
        assertNotNull(result.getFileName());
        assertTrue(result.getFileName().endsWith(".png"));
    }

    @Test
    void testUploadAvatar_NullFile() {
        assertThrows(AgentStudioException.class,
            () -> commonManagementService.uploadAvatar("proj-1", null));
    }

    @Test
    void testUploadAvatar_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]);

        assertThrows(AgentStudioException.class,
            () -> commonManagementService.uploadAvatar("proj-1", file));
    }

    @Test
    void testUploadAvatar_FileTooLarge() {
        byte[] largeContent = new byte[2 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", largeContent);

        assertThrows(AgentStudioException.class,
            () -> commonManagementService.uploadAvatar("proj-1", file));
    }

    @Test
    void testUploadAvatar_InvalidFileName() {
        MockMultipartFile file = new MockMultipartFile("file", "../evil.png", "image/png", "test".getBytes());

        assertThrows(AgentStudioException.class,
            () -> commonManagementService.uploadAvatar("proj-1", file));
    }

    @Test
    void testUploadAvatar_UnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.exe", "application/exe", "test".getBytes());

        assertThrows(AgentStudioException.class,
            () -> commonManagementService.uploadAvatar("proj-1", file));
    }

    @Test
    void testListTags_Success() {
        Tag tag1 = new Tag();
        tag1.setTagId("tag-1");
        tag1.setName("Tag1");
        Tag tag2 = new Tag();
        tag2.setTagId("tag-2");
        tag2.setName("Tag2");

        when(tagMapper.select()).thenReturn(List.of(tag1, tag2));

        List<TagInfo> result = commonManagementService.listTags("proj-1", "ws-1");
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testListTags_Empty() {
        when(tagMapper.select()).thenReturn(new ArrayList<>());

        List<TagInfo> result = commonManagementService.listTags("proj-1", "ws-1");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testInit_ParsesConfigurations() {
        ReflectionTestUtils.setField(commonManagementService, "frontPageBlockNodes", "node1,node2,node3");
        ReflectionTestUtils.setField(commonManagementService, "codeEnvOptions", "[\"python3\",\"nodejs\"]");

        commonManagementService.init();

        assertNotNull(commonManagementService);
    }
}
