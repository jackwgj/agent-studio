/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.filter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.entity.WorkSpaceMemberEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMapper;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMemberMapper;
import com.openjiuwen.studio.agent.manager.service.PermissionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.web.servlet.HandlerMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class WorkspaceInterceptorTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkspaceMapper workspaceMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkspaceMemberMapper workSpaceMemberMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PermissionService permissionService;

    @InjectMocks
    private WorkspaceInterceptor workspaceInterceptor;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_preHandle_should_return_true() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn(
                "001");

            WorkSpaceMemberEntity workSpaceMemberEntity = new WorkSpaceMemberEntity();
            when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString())).thenReturn(
                workSpaceMemberEntity);

            List<WorkspaceEntity> workspaces = new ArrayList<>();
            WorkspaceEntity workspaceEntity = new WorkspaceEntity();
            workspaceEntity.setProjectId("project_mock");
            workspaceEntity.setId("default");
            workspaces.add(workspaceEntity);
            when(workspaceMapper.getWorkspaceByProjectId(anyString())).thenReturn(workspaces);

            HttpServletRequest request = mock(HttpServletRequest.class, Answers.RETURNS_DEEP_STUBS);

            HttpServletResponse response = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

            Object handler = new Object();

            // 模拟request的getAttribute方法，使其返回一个包含project_id的Map
            Map<String, String> pathVariables = new HashMap<>();
            pathVariables.put("project_id", "123");
            Mockito.when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(pathVariables);

            Mockito.when(request.getParameter("workspace_id"))
                .thenReturn("default");

            List<String> actions = List.of("action1","action2");
            Mockito.when(permissionService.getMergedPermissions().get("REGISTERED_URIS"))
                .thenReturn(actions);

            // When
            boolean result = workspaceInterceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);

            when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString())).thenReturn(
                null);

            assertThrows(AgentStudioException.class, () -> {
                workspaceInterceptor.preHandle(request, response, handler);
            });

            when(workspaceMapper.getWorkspaceByProjectId(anyString())).thenReturn(null);

            assertThrows(AgentStudioException.class, () -> {
                workspaceInterceptor.preHandle(request, response, handler);
            });

            Mockito.when(request.getParameter("workspace_id"))
                .thenReturn("default@");
            assertThrows(AgentStudioException.class, () -> {
                workspaceInterceptor.preHandle(request, response, handler);
            });
        }
    }

    @Test
    public void extractProjectIdTest(){
        HttpServletRequest request = mock(HttpServletRequest.class, Answers.RETURNS_DEEP_STUBS);
        Mockito.when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
            .thenThrow(new RuntimeException(""));
        Assertions.assertEquals("", new WorkspaceInterceptor().extractProjectId(request));
    }
}
