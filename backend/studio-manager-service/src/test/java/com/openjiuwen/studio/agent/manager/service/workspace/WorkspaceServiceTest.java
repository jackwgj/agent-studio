/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.workspace;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.CreateWorkspaceReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteWorkspaceReq;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceListRsp;
import com.openjiuwen.studio.agent.manager.dto.MemberRole;
import com.openjiuwen.studio.agent.manager.dto.QueryWorkspaceQo;
import com.openjiuwen.studio.agent.manager.dto.UpdateWorkspaceReq;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceInfo;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberInfo;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMapper;
import com.openjiuwen.studio.agent.manager.service.AgentManagementService;
import com.openjiuwen.studio.agent.manager.service.SkuManageService;
import com.openjiuwen.studio.agent.manager.service.WorkflowManagementService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceMapper workspaceMapper;
    @Mock
    private WorkspaceMappingService workspaceMappingService;
    @Mock
    private WorkspaceMemberService workspaceMemberService;
    @Mock
    private SkuManageService skuManageService;
    @Mock
    private AgentManagementService agentManagementService;
    @Mock
    private WorkflowManagementService workflowManagementService;

    @InjectMocks
    private WorkspaceService workspaceService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workspaceService, "workSpaceDefaultIcon", "default-icon");
        ReflectionTestUtils.setField(workspaceService, "iconMaxSize", "1048576");
        ReflectionTestUtils.setField(workspaceService, "allowedIconTypeStr", "png,jpg,jpeg");
        ReflectionTestUtils.setField(workspaceService, "agentInitTemplateEnable", false);
        ReflectionTestUtils.setField(workspaceService, "agentInitTemplatePath", "");
        workspaceService.init();
    }

    @Test
    void testCreateWorkspace_Success() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");
            ctx.when(RequestContextUtils::getRequestUserName).thenReturn("user1");
            ctx.when(RequestContextUtils::getRequestUserId).thenReturn("uid-1");

            when(workspaceMapper.countWorkspaceByDomainId(anyString(), anyString())).thenReturn(0);
            when(workspaceMapper.countWorkspaceEntityByName(anyString(), anyString())).thenReturn(0);

            CreateWorkspaceReq req = new CreateWorkspaceReq();
            req.setName("Test Workspace");
            req.setDescription("desc");

            String result = workspaceService.createWorkspace("p1", req);

            assertNotNull(result);
            verify(workspaceMapper).insert(any(WorkspaceEntity.class));
            verify(workspaceMemberService).addWorkspaceMember(anyString(), eq("uid-1"), eq(MemberRole.OWNER.getValue()));
        }
    }

    @Test
    void testCreateWorkspace_DuplicateName() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            when(workspaceMapper.countWorkspaceByDomainId(anyString(), anyString())).thenReturn(0);
            when(workspaceMapper.countWorkspaceEntityByName(anyString(), anyString())).thenReturn(1);

            CreateWorkspaceReq req = new CreateWorkspaceReq();
            req.setName("Existing Workspace");

            assertThrows(AgentStudioException.class, () ->
                workspaceService.createWorkspace("p1", req));
        }
    }

    @Test
    void testUpdateWorkspace_Success() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserId).thenReturn("uid-1");
            ctx.when(RequestContextUtils::getRequestUserName).thenReturn("user1");

            WorkspaceEntity existing = new WorkspaceEntity();
            existing.setId("ws-1");
            existing.setName("Old Name");
            when(workspaceMapper.selectWorkspaceEntityById("p1", "ws-1")).thenReturn(existing);

            WorkspaceMemberInfo memberInfo = new WorkspaceMemberInfo();
            memberInfo.setRole(MemberRole.OWNER.getValue());
            when(workspaceMemberService.queryWorkspaceMemberDetail(eq("p1"), eq("uid-1"), eq("ws-1")))
                .thenReturn(memberInfo);
            when(workspaceMapper.countWorkspaceEntityByName(anyString(), anyString())).thenReturn(0);

            UpdateWorkspaceReq req = new UpdateWorkspaceReq();
            req.setId("ws-1");
            req.setName("New Name");
            req.setDescription("new desc");

            WorkspaceInfo result = workspaceService.updateWorkspace("p1", req);

            assertNotNull(result);
            verify(workspaceMapper).updateByPrimaryKeySelective(any(WorkspaceEntity.class));
        }
    }

    @Test
    void testUpdateWorkspace_NotFound() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserId).thenReturn("uid-1");

            when(workspaceMapper.selectWorkspaceEntityById("p1", "ws-1")).thenReturn(null);

            UpdateWorkspaceReq req = new UpdateWorkspaceReq();
            req.setId("ws-1");
            req.setName("New Name");

            assertThrows(AgentStudioException.class, () ->
                workspaceService.updateWorkspace("p1", req));
        }
    }

    @Test
    void testUpdateWorkspace_NoPermission() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserId).thenReturn("uid-1");

            WorkspaceEntity existing = new WorkspaceEntity();
            existing.setId("ws-1");
            existing.setName("Old Name");
            when(workspaceMapper.selectWorkspaceEntityById("p1", "ws-1")).thenReturn(existing);
            when(workspaceMemberService.queryWorkspaceMemberDetail(eq("p1"), eq("uid-1"), eq("ws-1")))
                .thenReturn(null);

            UpdateWorkspaceReq req = new UpdateWorkspaceReq();
            req.setId("ws-1");
            req.setName("New Name");

            assertThrows(AgentStudioException.class, () ->
                workspaceService.updateWorkspace("p1", req));
        }
    }

    @Test
    void testDeleteWorkspace_Success() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserId).thenReturn("uid-1");

            WorkspaceMemberInfo memberInfo = new WorkspaceMemberInfo();
            memberInfo.setRole(MemberRole.OWNER.getValue());
            when(workspaceMemberService.queryWorkspaceMemberDetail(eq("p1"), eq("uid-1"), eq("ws-1")))
                .thenReturn(memberInfo);

            WorkspaceInfo wsInfo = new WorkspaceInfo();
            wsInfo.setType("team");
            when(workspaceMapper.selectById("p1", "ws-1")).thenReturn(wsInfo);

            DeleteWorkspaceReq req = new DeleteWorkspaceReq();
            req.setId("ws-1");

            String result = workspaceService.deleteWorkspace("p1", req);

            assertEquals("ws-1", result);
            verify(workspaceMapper).updateByPrimaryKeySelective(any(WorkspaceEntity.class));
        }
    }

    @Test
    void testDeleteWorkspace_NoPermission() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserId).thenReturn("uid-1");

            when(workspaceMemberService.queryWorkspaceMemberDetail(eq("p1"), eq("uid-1"), eq("ws-1")))
                .thenReturn(null);

            DeleteWorkspaceReq req = new DeleteWorkspaceReq();
            req.setId("ws-1");

            assertThrows(AgentStudioException.class, () ->
                workspaceService.deleteWorkspace("p1", req));
        }
    }

    @Test
    void testDeleteWorkspace_PersonalType() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserId).thenReturn("uid-1");

            WorkspaceMemberInfo memberInfo = new WorkspaceMemberInfo();
            memberInfo.setRole(MemberRole.OWNER.getValue());
            when(workspaceMemberService.queryWorkspaceMemberDetail(eq("p1"), eq("uid-1"), eq("ws-1")))
                .thenReturn(memberInfo);

            WorkspaceInfo wsInfo = new WorkspaceInfo();
            wsInfo.setType("PERSON");
            when(workspaceMapper.selectById("p1", "ws-1")).thenReturn(wsInfo);

            DeleteWorkspaceReq req = new DeleteWorkspaceReq();
            req.setId("ws-1");

            assertThrows(AgentStudioException.class, () ->
                workspaceService.deleteWorkspace("p1", req));
        }
    }

    @Test
    void testQueryWorkspace_ProjectScope() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserId).thenReturn("uid-1");

            QueryWorkspaceQo qo = new QueryWorkspaceQo();
            qo.setScope("project");

            when(workspaceMapper.selectWorkspaceWithMappingInfo(eq("p1"), any()))
                .thenReturn(Collections.emptyList());

            GetWorkspaceListRsp result = workspaceService.queryWorkspace("p1", qo);

            assertNotNull(result);
            assertEquals(0, result.getCount());
        }
    }

    @Test
    void testValidateIcon_NullIcon() {
        assertDoesNotThrow(() -> workspaceService.validateIcon(null));
    }

    @Test
    void testValidateIcon_EmptyIcon() {
        assertDoesNotThrow(() -> workspaceService.validateIcon(""));
    }
}
