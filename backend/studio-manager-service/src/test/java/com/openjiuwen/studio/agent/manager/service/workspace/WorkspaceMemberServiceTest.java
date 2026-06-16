/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.AddWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceMemberListRsp;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceMemberRoleRsp;
import com.openjiuwen.studio.agent.manager.dto.MemberRole;
import com.openjiuwen.studio.agent.manager.dto.QueryWorkspaceMemberListQo;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceInfo;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberBody;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberBody1;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberInfo;
import com.openjiuwen.studio.agent.manager.dto.UpdateWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.MemberOwnershipBody;
import com.openjiuwen.studio.agent.manager.dto.CommonResponse;
import com.openjiuwen.studio.agent.manager.dto.iam.GetIamUserResponse;
import com.openjiuwen.studio.agent.manager.dto.iam.IamUserInfo;
import com.openjiuwen.studio.agent.manager.entity.WorkSpaceMemberEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMapper;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMemberMapper;
import com.openjiuwen.studio.agent.manager.service.SkuManageService;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceMemberServiceTest {

    @Mock
    private WorkspaceMemberMapper workspaceMemberMapper;

    @Mock
    private WorkspaceMapper workspaceMapper;

    @Mock
    private SkuManageService skuManageService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IamServiceUtils iamServiceUtils;

    @Mock
    private I18nUtil i18nUtil;

    @InjectMocks
    private WorkspaceMemberService workspaceMemberService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workspaceMemberService, "roleListStr", "");
    }

    @Test
    void testQueryRoleList_BlankRoleStr() {
        GetWorkspaceMemberRoleRsp result = workspaceMemberService.queryRoleList("proj1");
        assertNotNull(result);
        assertNull(result.getRoleList());
    }

    @Test
    void testQueryRoleList_ValidRoleStr() throws Exception {
        ReflectionTestUtils.setField(workspaceMemberService, "roleListStr",
            "[{\"roleName\":\"admin\",\"roleValue\":\"admin\"}]");
        ReflectionTestUtils.setField(workspaceMemberService, "objectMapper", new ObjectMapper());

        GetWorkspaceMemberRoleRsp result = workspaceMemberService.queryRoleList("proj1");
        assertNotNull(result);
        assertNotNull(result.getRoleList());
        assertEquals(1, result.getRoleList().size());
    }

    @Test
    void testQueryRoleList_InvalidJson() throws Exception {
        ReflectionTestUtils.setField(workspaceMemberService, "roleListStr", "invalid json");
        ReflectionTestUtils.setField(workspaceMemberService, "objectMapper", new ObjectMapper());

        GetWorkspaceMemberRoleRsp result = workspaceMemberService.queryRoleList("proj1");
        assertNotNull(result);
        assertNull(result.getRoleList());
    }

    @Test
    void testQueryWorkspaceMemberDetail_Found() {
        WorkSpaceMemberEntity entity = new WorkSpaceMemberEntity();
        entity.setMemberId("user1");
        entity.setMemberName("Test User");
        entity.setRole("admin");
        when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(entity);

        WorkspaceMemberInfo result = workspaceMemberService.queryWorkspaceMemberDetail("proj1", "user1", "ws1");
        assertNotNull(result);
        assertEquals("user1", result.getMemberId());
    }

    @Test
    void testQueryWorkspaceMemberDetail_NotFound() {
        when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(null);

        WorkspaceMemberInfo result = workspaceMemberService.queryWorkspaceMemberDetail("proj1", "user1", "ws1");
        assertNotNull(result);
        assertNull(result.getMemberId());
    }

    @Test
    void testQueryIamUserList_EmptyUsers() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            when(iamServiceUtils.queryDomainTokenFromCache("")).thenReturn("token");
            GetIamUserResponse response = new GetIamUserResponse();
            response.setUsers(null);
            when(iamServiceUtils.queryIamUserList("token")).thenReturn(response);

            GetWorkspaceMemberListRsp result = workspaceMemberService.queryIamUserList("proj1");
            assertNotNull(result);
        }
    }

    @Test
    void testQueryIamUserList_WithUsers() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            when(iamServiceUtils.queryDomainTokenFromCache("")).thenReturn("token");
            IamUserInfo iamUser = new IamUserInfo();
            iamUser.setName("user1");
            iamUser.setId("id1");
            GetIamUserResponse response = new GetIamUserResponse();
            response.setUsers(List.of(iamUser));
            when(iamServiceUtils.queryIamUserList("token")).thenReturn(response);

            GetWorkspaceMemberListRsp result = workspaceMemberService.queryIamUserList("proj1");
            assertNotNull(result);
            assertEquals(1, result.getCount());
        }
    }

    @Test
    void testQueryAllWorkSpaceInfoByIamUserId() {
        WorkSpaceMemberEntity entity = new WorkSpaceMemberEntity();
        when(workspaceMemberMapper.selectAllWorkspaceByMemberId("user1")).thenReturn(List.of(entity));

        List<WorkSpaceMemberEntity> result = workspaceMemberService.queryAllWorkSpaceInfoByIamUserId("user1");
        assertEquals(1, result.size());
    }

    @Test
    void testCheckMemberOwnerOrAdminPermission_WorkspaceNotExist() {
        when(workspaceMapper.getWorkspaceByWorkspaceId("proj1", "ws1")).thenReturn(null);

        assertThrows(AgentStudioException.class, () ->
            workspaceMemberService.checkMemberOwnerOrAdminPermission("proj1", "ws1", "user1"));
    }

    @Test
    void testCheckMemberOwnerOrAdminPermission_InvalidWorkspaceType() {
        WorkspaceEntity ws = new WorkspaceEntity();
        ws.setType("personal");
        when(workspaceMapper.getWorkspaceByWorkspaceId("proj1", "ws1")).thenReturn(ws);

        assertThrows(AgentStudioException.class, () ->
            workspaceMemberService.checkMemberOwnerOrAdminPermission("proj1", "ws1", "user1"));
    }

    @Test
    void testCheckMemberOwnerOrAdminPermission_NotOwnerOrAdmin() {
        WorkspaceEntity ws = new WorkspaceEntity();
        ws.setType("team");
        when(workspaceMapper.getWorkspaceByWorkspaceId("proj1", "ws1")).thenReturn(ws);

        WorkSpaceMemberEntity member = new WorkSpaceMemberEntity();
        member.setRole("viewer");
        when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(member);

        assertThrows(AgentStudioException.class, () ->
            workspaceMemberService.checkMemberOwnerOrAdminPermission("proj1", "ws1", "user1"));
    }

    @Test
    void testCheckMemberOwnerOrAdminPermission_Owner() {
        WorkspaceEntity ws = new WorkspaceEntity();
        ws.setType("team");
        when(workspaceMapper.getWorkspaceByWorkspaceId("proj1", "ws1")).thenReturn(ws);

        WorkSpaceMemberEntity member = new WorkSpaceMemberEntity();
        member.setRole(MemberRole.OWNER.getValue());
        when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(member);

        WorkSpaceMemberEntity result = workspaceMemberService.checkMemberOwnerOrAdminPermission("proj1", "ws1", "user1");
        assertNotNull(result);
        assertEquals(MemberRole.OWNER.getValue(), result.getRole());
    }

    @Test
    void testAddWorkspaceMember_AlreadyExists() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");
            mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn("User 1");
            mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain1");

            WorkSpaceMemberEntity existing = new WorkSpaceMemberEntity();
            existing.setMemberName("Existing User");
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("iam1", "ws1")).thenReturn(existing);

            assertThrows(AgentStudioException.class, () ->
                workspaceMemberService.addWorkspaceMember("ws1", "iam1", "admin"));
        }
    }

    @Test
    void testAddWorkspaceMember_Success() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");
            mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn("User 1");
            mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain1");

            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("iam1", "ws1")).thenReturn(null);

            workspaceMemberService.addWorkspaceMember("ws1", "iam1", "admin");
            verify(workspaceMemberMapper).insertWorkspaceMember(any(WorkSpaceMemberEntity.class));
        }
    }

    @Test
    void testDeleteWorkspaceMember_NotExist() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");
            mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn("User 1");

            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("iam1", "ws1")).thenReturn(null);

            assertThrows(AgentStudioException.class, () ->
                workspaceMemberService.deleteWorkspaceMember("ws1", "iam1"));
        }
    }

    @Test
    void testDeleteWorkspaceMember_Success() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");
            mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn("User 1");

            WorkSpaceMemberEntity entity = new WorkSpaceMemberEntity();
            entity.setMemberId("iam1");
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("iam1", "ws1")).thenReturn(entity);

            workspaceMemberService.deleteWorkspaceMember("ws1", "iam1");
            verify(workspaceMemberMapper).deleteWorkspaceMemberByMemberIdWorkspaceId(any(WorkSpaceMemberEntity.class));
        }
    }

    @Test
    void testExitWorkspace_PersonalType() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");

            WorkspaceInfo wsInfo = new WorkspaceInfo();
            wsInfo.setType("personal");
            when(workspaceMapper.selectById("proj1", "ws1")).thenReturn(wsInfo);

            assertThrows(AgentStudioException.class, () ->
                workspaceMemberService.exitWorkspace("proj1", "ws1"));
        }
    }

    @Test
    void testExitWorkspace_OwnerCannotExit() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");

            WorkspaceInfo wsInfo = new WorkspaceInfo();
            wsInfo.setType("team");
            when(workspaceMapper.selectById("proj1", "ws1")).thenReturn(wsInfo);

            WorkSpaceMemberEntity member = new WorkSpaceMemberEntity();
            member.setRole(MemberRole.OWNER.getValue());
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(member);

            assertThrows(AgentStudioException.class, () ->
                workspaceMemberService.exitWorkspace("proj1", "ws1"));
        }
    }

    @Test
    void testExitWorkspace_Success() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");
            mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn("User 1");

            WorkspaceInfo wsInfo = new WorkspaceInfo();
            wsInfo.setType("team");
            when(workspaceMapper.selectById("proj1", "ws1")).thenReturn(wsInfo);

            WorkSpaceMemberEntity member = new WorkSpaceMemberEntity();
            member.setRole(MemberRole.ADMIN.getValue());
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(member);

            CommonResponse result = workspaceMemberService.exitWorkspace("proj1", "ws1");
            assertEquals(200, result.getCode());
        }
    }

    @Test
    void testBatchAddWorkspaceMember_EmptyMembers() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            WorkspaceMemberBody body = new WorkspaceMemberBody();
            body.setMembers(new ArrayList<>());

            Integer result = workspaceMemberService.batchAddWorkspaceMember("proj1", "ws1", body);
            assertEquals(0, result);
        }
    }

    @Test
    void testBatchDeleteWorkspaceMember_EmptyUserIds() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");

            WorkspaceEntity ws = new WorkspaceEntity();
            ws.setType("team");
            when(workspaceMapper.getWorkspaceByWorkspaceId("proj1", "ws1")).thenReturn(ws);

            WorkSpaceMemberEntity currentMember = new WorkSpaceMemberEntity();
            currentMember.setRole(MemberRole.OWNER.getValue());
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(currentMember);

            DeleteWorkspaceMemberReq body = new DeleteWorkspaceMemberReq();
            body.setUserIds(new ArrayList<>());

            Integer result = workspaceMemberService.batchDeleteWorkspaceMember("proj1", "ws1", body);
            assertEquals(0, result);
        }
    }

    @Test
    void testBatchUpdateWorkspaceMemberRole_EmptyMembers() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");

            WorkspaceEntity ws = new WorkspaceEntity();
            ws.setType("team");
            when(workspaceMapper.getWorkspaceByWorkspaceId("proj1", "ws1")).thenReturn(ws);

            WorkSpaceMemberEntity currentMember = new WorkSpaceMemberEntity();
            currentMember.setRole(MemberRole.OWNER.getValue());
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(currentMember);

            WorkspaceMemberBody1 body = new WorkspaceMemberBody1();
            body.setMembers(new ArrayList<>());

            Integer result = workspaceMemberService.batchUpdateWorkspaceMemberRole("ws1", "proj1", body);
            assertEquals(0, result);
        }
    }

    @Test
    void testTransferWorkspaceOwnership_NotOwner() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");

            WorkSpaceMemberEntity member = new WorkSpaceMemberEntity();
            member.setRole(MemberRole.ADMIN.getValue());
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(member);

            MemberOwnershipBody body = new MemberOwnershipBody();
            body.setWorkspaceId("ws1");
            body.setNextOwnerId("user2");

            assertThrows(AgentStudioException.class, () ->
                workspaceMemberService.transferWorkspaceOwnership("proj1", body));
        }
    }

    @Test
    void testTransferWorkspaceOwnership_NextOwnerNotExist() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");
            mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn("User 1");

            WorkSpaceMemberEntity owner = new WorkSpaceMemberEntity();
            owner.setRole(MemberRole.OWNER.getValue());
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(owner);
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user2", "ws1")).thenReturn(null);

            MemberOwnershipBody body = new MemberOwnershipBody();
            body.setWorkspaceId("ws1");
            body.setNextOwnerId("user2");

            assertThrows(AgentStudioException.class, () ->
                workspaceMemberService.transferWorkspaceOwnership("proj1", body));
        }
    }

    @Test
    void testTransferWorkspaceOwnership_Success() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user1");
            mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn("User 1");

            WorkSpaceMemberEntity owner = new WorkSpaceMemberEntity();
            owner.setRole(MemberRole.OWNER.getValue());
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user1", "ws1")).thenReturn(owner);

            WorkSpaceMemberEntity nextOwner = new WorkSpaceMemberEntity();
            nextOwner.setRole(MemberRole.ADMIN.getValue());
            when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user2", "ws1")).thenReturn(nextOwner);

            MemberOwnershipBody body = new MemberOwnershipBody();
            body.setWorkspaceId("ws1");
            body.setNextOwnerId("user2");

            CommonResponse result = workspaceMemberService.transferWorkspaceOwnership("proj1", body);
            assertEquals(200, result.getCode());
            assertEquals("user2", result.getData());
            verify(workspaceMemberMapper, times(2)).updateMemberRole(any(WorkSpaceMemberEntity.class));
        }
    }
}
