/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateDto;
import com.openjiuwen.studio.prompt.engineering.entity.Industry;
import com.openjiuwen.studio.prompt.engineering.entity.PePromptTemplate;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptTemplateMapper;
import com.openjiuwen.studio.prompt.engineering.utils.ExcelI18nHandler;

import cn.afterturn.easypoi.handler.inter.II18nHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PromptTemplateServiceTest {

    @Mock
    private PePromptTemplateMapper pePromptTemplateMapper;

    @Mock
    private PromptTransactionService conversionTransaction;

    @InjectMocks
    private PromptTemplateService promptTemplateService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private II18nHandler i18nHandler = new ExcelI18nHandler(messageSource);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(promptTemplateService, "templateQuota", 500);
        ReflectionTestUtils.setField(promptTemplateService, "templateDownloadQuota", 100);
    }

    @Test
    void testSavePromptTemplate_Success() {
        // Arrange
        String projectId = "project1";
        String workspaceId = "workspace1";
        String name = "Template1";
        PePromptTemplateDto promptTemplateDto = new PePromptTemplateDto();
        promptTemplateDto.setName(name);
        promptTemplateDto.setTags(Collections.singletonList("tag1"));
        promptTemplateDto.setSource("PRESET");

        when(pePromptTemplateMapper.isNameExist(anyString(), anyString(), anyString(), anyString())).thenReturn(false);
        when(pePromptTemplateMapper.countTemplateByProjectId(anyString(), anyString())).thenReturn(10);
        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("test");
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            mocked.when(RequestContextUtils::getRequestUser).thenReturn(userInfo);

            // Act
            String result = promptTemplateService.savePromptTemplate(projectId, workspaceId, promptTemplateDto);

            // Assert
            assertEquals(HttpStatus.OK.toString(), result);
            verify(pePromptTemplateMapper).isNameExist(name, null, projectId, workspaceId);
            verify(pePromptTemplateMapper).countTemplateByProjectId(projectId, workspaceId);
            verify(conversionTransaction).insertOneTemplate(any(PePromptTemplate.class), anyList(), eq(projectId));
        }
    }

    @Test
    void testSavePromptTemplate_ExceedsQuota() {
        // Arrange
        String projectId = "project1";
        String workspaceId = "workspace1";
        String name = "Template1";
        PePromptTemplateDto promptTemplateDto = new PePromptTemplateDto();
        promptTemplateDto.setName(name);

        when(pePromptTemplateMapper.isNameExist(anyString(), anyString(), anyString(), anyString())).thenReturn(false);
        when(pePromptTemplateMapper.countTemplateByProjectId(anyString(), anyString())).thenReturn(500);
        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("test");
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            mocked.when(RequestContextUtils::getRequestUser).thenReturn(userInfo);

            // Act & Assert
            assertThrows(AgentStudioException.class,
                () -> promptTemplateService.savePromptTemplate(projectId, workspaceId, promptTemplateDto));
            verify(pePromptTemplateMapper).isNameExist(name, null, projectId, workspaceId);
            verify(pePromptTemplateMapper).countTemplateByProjectId(projectId, workspaceId);
        }
    }

    @Test
    public void test_downloadPromptTemplateV2() {
        // Given
        String projectId = "project-123";
        String workspaceId = "workspace-123";
        List<String> templateIds = new ArrayList<>();
        templateIds.add("template");

        // 模拟 TemplateDownloadVo 数据
        PePromptTemplate vo1 = new PePromptTemplate();
        vo1.setName("模板1");
        vo1.setContent("内容1");
        vo1.setDescription("描述1");
        vo1.setVariables("变量1");
        vo1.setIndustry(new Industry());
        vo1.setCreator("创建人1");

        List<PePromptTemplate> templateDownloadVos = new ArrayList<>();
        templateDownloadVos.add(vo1);

        // 模拟 queryDownloadVoList
        when(pePromptTemplateMapper.queryTemplateDetailByCreatorTemplateIds(anyList())).thenReturn(templateDownloadVos);
        when(pePromptTemplateMapper.queryTemplateDetailByPersonTemplateIds(anyList(), anyString())).thenReturn(
            templateDownloadVos);

        // When
        assertThrows(AgentStudioException.class, () -> {
            promptTemplateService.downloadPromptTemplateV2(projectId, workspaceId, templateIds);
        });
    }
}
