/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.workspace;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.AddWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.CommonResponse;
import com.openjiuwen.studio.agent.manager.dto.DeleteWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceMemberListRsp;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceMemberRoleRsp;
import com.openjiuwen.studio.agent.manager.dto.MemberOwnershipBody;
import com.openjiuwen.studio.agent.manager.dto.MemberRole;
import com.openjiuwen.studio.agent.manager.dto.QueryWorkspaceMemberListQo;
import com.openjiuwen.studio.agent.manager.dto.RoleInfo;
import com.openjiuwen.studio.agent.manager.dto.UpdateWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceInfo;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberBody;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberBody1;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberInfo;
import com.openjiuwen.studio.agent.manager.dto.iam.GetIamUserResponse;
import com.openjiuwen.studio.agent.manager.dto.iam.IamUserInfo;
import com.openjiuwen.studio.agent.manager.entity.WorkSpaceMemberEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMapper;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMemberMapper;
import com.openjiuwen.studio.agent.manager.service.SkuManageService;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MockitoSettings(strictness = Strictness.LENIENT)
class WorkspaceMemberServiceTest {

    @Mock
    private WorkspaceMemberMapper workSpaceMemberMapper;

    @Mock
    private WorkspaceMapper workspaceMapper;

    @Mock()
    private IamServiceUtils iamServiceUtils;

    @InjectMocks
    private WorkspaceMemberService workSpaceMemberService;

    @Mock
    private SkuManageService skuManageService;

    private AutoCloseable mockitoCloseable;

    private WorkSpaceMemberEntity member;

    private final String memberId = UUID.randomUUID().toString().replace("_", "");

    private static final String workspaceId = "0001";

    private static final String projectId = "project_mock";

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);
        when(workspaceMapper.getWorkspaceByWorkspaceId(workspaceId, projectId)).thenReturn(workspace);

        member = new WorkSpaceMemberEntity();
        member.setWorkspaceId(workspaceId)
            .setMemberId(memberId)
            .setMemberName("testUserName001")
            .setRole(MemberRole.OWNER.getValue());

        WorkspaceInfo workspaceInfo = new WorkspaceInfo();
        workspaceInfo.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);

        when(workspaceMapper.selectById(projectId, workspaceId))
            .thenReturn(workspaceInfo);

        when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(memberId, workspaceId)).thenReturn(member);

        ReflectionTestUtils.setField(workSpaceMemberService, "roleListStr", "");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void shouldReturnZero_whenAllMembersAlreadyExist() {
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            mockRequestContextUtils(mocked);
            when(workSpaceMemberMapper.batchInsertWorkspaceMember(anyList())).thenReturn(0);
            WorkspaceEntity workspaceInfo = new WorkspaceEntity();
            workspaceInfo.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);
            when(workspaceMapper.getWorkspaceByWorkspaceId(anyString(), anyString())).thenReturn(workspaceInfo);
            WorkspaceMemberBody body = createMemberBody(UUID.randomUUID().toString().replace("_", ""), "memberName001",
                "IAM");

            int result = workSpaceMemberService.batchAddWorkspaceMember(projectId, workspaceId, body);

            assertThat(result).as("No new members should be inserted if they already exist").isZero();
        }
    }

    private void mockRequestContextUtils(MockedStatic<RequestContextUtils> mocked) {
        mocked.when(RequestContextUtils::getRequestUserId).thenReturn(memberId);
    }

    private WorkspaceMemberBody createMemberBody(String memberId, String memberName, String source) {
        AddWorkspaceMemberReq memberReq = new AddWorkspaceMemberReq();
        memberReq.setMemberId(memberId);
        memberReq.setMemberName(memberName);
        memberReq.setMemberSource(source);
        memberReq.setRole(MemberRole.ADMIN);
        WorkspaceMemberBody body = new WorkspaceMemberBody();
        body.setMembers(Collections.singletonList(memberReq));
        return body;
    }

    @Test
    void test_batchAddWorkspaceMember_should_not_throw_exception() {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS)) {

                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(memberId);

                when(workSpaceMemberMapper.batchInsertWorkspaceMember(anyList())).thenReturn(0);

                WorkspaceEntity workspaceInfo = new WorkspaceEntity();
                workspaceInfo.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);
                when(workspaceMapper.getWorkspaceByWorkspaceId(projectId, workspaceId)).thenReturn(workspaceInfo);
                when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(memberId, workspaceId)).thenReturn(member);

                WorkspaceMemberBody workspaceMemberBody = new WorkspaceMemberBody();

                Integer result = workSpaceMemberService.batchAddWorkspaceMember(projectId, workspaceId,
                    workspaceMemberBody);
                assertEquals(0, result);
            }
        });
    }

    @Test
    void test_batchDeleteWorkspaceMember_should_equal_result() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName).thenReturn("not_empty");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("not_empty");

            when(workSpaceMemberMapper.batchDeleteWorkspaceMemberByIds(anyString(), anyList(), anyString(),
                anyString())).thenReturn(0);
            WorkSpaceMemberEntity existedMember = new WorkSpaceMemberEntity();
            when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString())).thenReturn(
                existedMember);
            existedMember.setRole(MemberRole.ADMIN.getValue());

            WorkspaceEntity workspaceInfo = new WorkspaceEntity();
            workspaceInfo.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);
            when(workspaceMapper.getWorkspaceByWorkspaceId(anyString(), anyString())).thenReturn(workspaceInfo);

            DeleteWorkspaceMemberReq body = new DeleteWorkspaceMemberReq();

            Integer result = workSpaceMemberService.batchDeleteWorkspaceMember("not_empty", "not_empty", body);

            assertEquals(0, result.intValue());
        }
    }

    @Test
    void test_batchDeleteWorkspaceMember_no_permission() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName).thenReturn("not_empty");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("not_empty");

            WorkSpaceMemberEntity existedMember = new WorkSpaceMemberEntity();
            when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString())).thenReturn(
                existedMember);
            existedMember.setRole(MemberRole.OWNER.getValue());

            WorkspaceEntity workspaceInfo = new WorkspaceEntity();
            workspaceInfo.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);
            when(workspaceMapper.getWorkspaceByWorkspaceId(anyString(), anyString())).thenReturn(workspaceInfo);

            DeleteWorkspaceMemberReq body = new DeleteWorkspaceMemberReq();
            body.setUserIds(Lists.newArrayList("not_empty"));

            workSpaceMemberService.batchDeleteWorkspaceMember("not_empty", "not_empty", body);
        }
    }

    @Test
    void test_batchDeleteWorkspaceMember_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(memberId);

                when(workSpaceMemberMapper.batchDeleteWorkspaceMemberByIds(anyString(), anyList(), anyString(),
                    anyString())).thenReturn(0);

                WorkspaceEntity workspaceInfo = new WorkspaceEntity();
                workspaceInfo.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);
                when(workspaceMapper.getWorkspaceByWorkspaceId(projectId, workspaceId)).thenReturn(workspaceInfo);

                DeleteWorkspaceMemberReq req = new DeleteWorkspaceMemberReq();
                Integer result = workSpaceMemberService.batchDeleteWorkspaceMember(projectId, workspaceId, req);
                assertEquals(0, result);
            }
        });
    }

    @Test
    void test_batchUpdateWorkspaceMemberRole_should_equal_result() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName).thenReturn("testUserName001");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(memberId);

            when(workSpaceMemberMapper.batchUpdateWorkspaceMemberRole(anyList())).thenReturn(0);
            WorkSpaceMemberEntity existedMember = new WorkSpaceMemberEntity();
            existedMember.setRole(MemberRole.ADMIN.getValue());
            when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(memberId, workspaceId)).thenReturn(existedMember);

            WorkspaceEntity workspaceInfo = new WorkspaceEntity();
            workspaceInfo.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);
            when(workspaceMapper.getWorkspaceByWorkspaceId(anyString(), anyString())).thenReturn(workspaceInfo);

            WorkspaceMemberBody1 body = new WorkspaceMemberBody1();

            Integer result = workSpaceMemberService.batchUpdateWorkspaceMemberRole(workspaceId, projectId, body);

            assertEquals(0, result);
        }
    }

    @Test
    void test_batchUpdateWorkspaceMemberRole_NO_permission() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName).thenReturn("testUserName001");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(memberId);

            when(workSpaceMemberMapper.batchUpdateWorkspaceMemberRole(anyList())).thenReturn(0);
            WorkSpaceMemberEntity existedMember = new WorkSpaceMemberEntity();
            existedMember.setRole(MemberRole.OWNER.getValue());
            when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(memberId, workspaceId)).thenReturn(existedMember);

            WorkspaceEntity workspaceInfo = new WorkspaceEntity();
            workspaceInfo.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);
            when(workspaceMapper.getWorkspaceByWorkspaceId(anyString(), anyString())).thenReturn(workspaceInfo);

            WorkspaceMemberBody1 body = new WorkspaceMemberBody1();
            UpdateWorkspaceMemberReq req = new UpdateWorkspaceMemberReq();
            body.setMembers(Lists.newArrayList(req));

            Assertions.assertThrows(AgentStudioException.class, () -> {
                workSpaceMemberService.batchUpdateWorkspaceMemberRole(workspaceId, projectId, body);
            });
        }
    }

    @Test
    void test_batchUpdateWorkspaceMemberRole_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName)
                    .thenReturn("testUserName001");
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(memberId);

                when(workSpaceMemberMapper.batchUpdateWorkspaceMemberRole(anyList())).thenReturn(0);

                WorkspaceEntity workspace = new WorkspaceEntity();
                workspace.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);
                when(workspaceMapper.getWorkspaceByWorkspaceId(projectId, workspaceId)).thenReturn(workspace);

                WorkspaceMemberBody1 body = new WorkspaceMemberBody1();
                List<UpdateWorkspaceMemberReq> memberList = new ArrayList<>();
                UpdateWorkspaceMemberReq req = new UpdateWorkspaceMemberReq();
                req.setMemberId(memberId);
                req.setRole(MemberRole.DEVELOPER);
                body.setMembers(memberList);

                WorkSpaceMemberEntity existedMember = new WorkSpaceMemberEntity();
                existedMember.setMemberId(memberId);
                existedMember.setRole(MemberRole.ADMIN.getValue());

                when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(projectId, workspaceId)).thenReturn(
                    existedMember);

                Integer result = workSpaceMemberService.batchUpdateWorkspaceMemberRole(workspaceId, projectId, body);

                assertEquals(0, result);
            }
        });
    }

    @Test
    void test_batchUpdateWorkspaceMemberRole_should_equal_result6() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {

            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName).thenReturn("testUserName001");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(memberId);
            WorkspaceEntity workspaceInfo = new WorkspaceEntity();
            workspaceInfo.setType(CommonConstant.WORKSPACE.TYPE.TEAM_TYPE);
            when(workspaceMapper.getWorkspaceByWorkspaceId(anyString(), anyString())).thenReturn(workspaceInfo);
            WorkspaceMemberBody1 body = new WorkspaceMemberBody1();
            Integer result = workSpaceMemberService.batchUpdateWorkspaceMemberRole(workspaceId, projectId, body);

            assertEquals(0, result);
        }
    }

    @Test
    void test_queryIamUserList_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("not_empty");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainName).thenReturn("not_empty");

            when(iamServiceUtils.queryDomainTokenFromCache(anyString())).thenReturn("not_empty");
            GetIamUserResponse response = new GetIamUserResponse();
            List<IamUserInfo> users = new ArrayList<>();
            IamUserInfo iamUserInfo = new IamUserInfo();
            users.add(iamUserInfo);
            response.setUsers(users);
            when(iamServiceUtils.queryIamUserList(anyString())).thenReturn(response);

            GetWorkspaceMemberListRsp result = workSpaceMemberService.queryIamUserList("not_empty");

            assertNotNull(result);
        }
    }

    @Test
    void test_queryRoleList_should_return_not_null() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
            // Given
            List<RoleInfo> roleInfoList = new ArrayList<>();
            RoleInfo roleInfo = new RoleInfo();
            roleInfoList.add(roleInfo);
            mockedStaticJSON.when(() -> JSON.parseArray(anyString(), eq(RoleInfo.class))).thenReturn(roleInfoList);

            GetWorkspaceMemberRoleRsp result = workSpaceMemberService.queryRoleList("not_empty");

            assertNotNull(result);
        }
    }

    @Test
    void test_queryRoleList_should_return_not_null1() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
            // Given
            List<RoleInfo> roleInfoList = new ArrayList<>();
            RoleInfo roleInfo = new RoleInfo();
            roleInfoList.add(roleInfo);
            mockedStaticJSON.when(() -> JSON.parseArray(anyString(), eq(RoleInfo.class))).thenReturn(roleInfoList);

            GetWorkspaceMemberRoleRsp result = workSpaceMemberService.queryRoleList("");

            assertNotNull(result);
        }
    }

    @Test
    void test_queryRoleList_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
                mockedStaticJSON.when(() -> JSON.parseArray(anyString(), eq(RoleInfo.class))).thenReturn(null);

                GetWorkspaceMemberRoleRsp result = workSpaceMemberService.queryRoleList(null);
            }
        });
    }

    @Test
    void test_queryRoleList_should_return_not_null3() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
            mockedStaticJSON.when(() -> JSON.parseArray(anyString(), eq(RoleInfo.class))).thenReturn(null);

            GetWorkspaceMemberRoleRsp result = workSpaceMemberService.queryRoleList(null);

            assertNotNull(result);
        }
    }

    @Test
    void test_queryWorkspaceMemberDetail_should_return_not_null() throws Exception {
        WorkSpaceMemberEntity workSpaceMemberEntity = new WorkSpaceMemberEntity();
        when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString())).thenReturn(
            workSpaceMemberEntity);

        WorkspaceMemberInfo result = workSpaceMemberService.queryWorkspaceMemberDetail("not_empty", "not_empty",
            "not_empty");

        assertNotNull(result);
    }

    @Test
    void test_queryWorkspaceMemberDetail_should_not_throw_exception() {
        assertDoesNotThrow(() -> {
            when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString())).thenReturn(null);

            WorkspaceMemberInfo result = workSpaceMemberService.queryWorkspaceMemberDetail(null, null, null);
        });
    }

    @Test
    void test_queryWorkspaceMemberDetail_should_return_not_null2() throws Exception {
        when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString())).thenReturn(null);

        WorkspaceMemberInfo result = workSpaceMemberService.queryWorkspaceMemberDetail(null, null, null);

        assertNotNull(result);
    }

    @Test
    void test_queryWorkspaceMemberList_should_return_not_null() throws Exception {
        QueryWorkspaceMemberListQo queryWorkspaceMemberListQo = new QueryWorkspaceMemberListQo();
        GetWorkspaceMemberListRsp result = workSpaceMemberService.queryWorkspaceMemberList("not_empty",
            queryWorkspaceMemberListQo);
        assertNotNull(result);
    }

    @Test
    void test_queryWorkspaceMemberList_should_not_throw_exception() throws Exception {
        QueryWorkspaceMemberListQo req = new QueryWorkspaceMemberListQo();
        req.setWorkspaceId("0001").setPageNum(1).setPageNum(10);
        assertDoesNotThrow(() -> {
            GetWorkspaceMemberListRsp result = workSpaceMemberService.queryWorkspaceMemberList(projectId, req);
        });
    }

    @Test
    void test_queryWorkspaceMemberList_should_return_not_null1() throws Exception {
        QueryWorkspaceMemberListQo req = new QueryWorkspaceMemberListQo();
        req.setWorkspaceId(workspaceId).setPageNum(1).setPageNum(10);
        GetWorkspaceMemberListRsp result = workSpaceMemberService.queryWorkspaceMemberList(workspaceId, req);

        assertNotNull(result);
    }

    @Test
    void test_addWorkspaceMember_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(memberId);
                WorkSpaceMemberEntity workSpaceMemberEntity = new WorkSpaceMemberEntity();
                when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(memberId, workspaceId)).thenReturn(
                    workSpaceMemberEntity);
                workSpaceMemberService.addWorkspaceMember(workspaceId, projectId, memberId);
            }
        });
    }

    @Test
    void test_addWorkspaceMember_should_not_throw_exception2() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(null);
                when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString())).thenReturn(null);
                workSpaceMemberService.addWorkspaceMember(null, null, null);
            }
        });
    }

    @Test
    void test_deleteWorkspaceMember_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("not_empty");
                when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString())).thenReturn(member);
                workSpaceMemberService.deleteWorkspaceMember("not_empty", "not_empty");
            }
        });
    }

    @Test
    void test_deleteWorkspaceMember_should_not_throw_exception2() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(null);
                when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(memberId, workspaceId)).thenReturn(member);
                workSpaceMemberService.deleteWorkspaceMember(workspaceId, memberId);
            }
        });
    }

    @Test
    void test_queryAllWorkSpaceInfoByIamUserId_should_return_not_null() throws Exception {
        List<WorkSpaceMemberEntity> list = new ArrayList<>();
        WorkSpaceMemberEntity workSpaceMemberEntity = new WorkSpaceMemberEntity();
        list.add(workSpaceMemberEntity);
        when(workSpaceMemberMapper.selectAllWorkspaceByMemberId(anyString())).thenReturn(list);
        List<WorkSpaceMemberEntity> result = workSpaceMemberService.queryAllWorkSpaceInfoByIamUserId("not_empty");
        assertNotNull(result);
    }

    @Test
    void test_queryAllWorkSpaceInfoByIamUserId_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            when(workSpaceMemberMapper.selectAllWorkspaceByMemberId(anyString())).thenReturn(null);
            List<WorkSpaceMemberEntity> result = workSpaceMemberService.queryAllWorkSpaceInfoByIamUserId(null);
        });
    }

    @Test
    void test_queryAllWorkSpaceInfoByIamUserId_should_return_not_null1() throws Exception {
        when(workSpaceMemberMapper.selectAllWorkspaceByMemberId(anyString())).thenReturn(null);
        List<WorkSpaceMemberEntity> result = workSpaceMemberService.queryAllWorkSpaceInfoByIamUserId(null);
        assertNotNull(result);
    }

    @Test
    void testTransferWorkspaceOwnership_Success() {
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            String nextOwnerId = UUID.randomUUID().toString().replace("_", "");
            MemberOwnershipBody body = new MemberOwnershipBody();
            body.setWorkspaceId(workspaceId);
            body.setNextOwnerId(nextOwnerId);

            mockRequestContextUtils(mocked);

            // 模拟下一任所有者信息
            WorkSpaceMemberEntity nextOwner = new WorkSpaceMemberEntity();
            nextOwner.setWorkspaceId(workspaceId)
                .setMemberId(nextOwnerId)
                .setRole(MemberRole.ADMIN.getValue());
            when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(nextOwnerId, workspaceId))
                .thenReturn(nextOwner);

            // 执行方法
            CommonResponse response = workSpaceMemberService.transferWorkspaceOwnership(projectId, body);

            // 验证结果
            assertEquals(200, response.getCode());
            assertEquals(nextOwnerId, response.getData());
            assertEquals(MemberRole.ADMIN.getValue(), member.getRole());
            assertEquals(MemberRole.OWNER.getValue(), nextOwner.getRole());
            verify(workSpaceMemberMapper, times(1)).updateMemberRole(member);
            verify(workSpaceMemberMapper, times(1)).updateMemberRole(nextOwner);
        }
    }

    @Test
    void testTransferWorkspaceOwnership_NotOwner() {
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            mockRequestContextUtils(mocked);
            member.setRole(MemberRole.ADMIN.getValue());
            MemberOwnershipBody body = new MemberOwnershipBody();
            body.setWorkspaceId(workspaceId);
            body.setNextOwnerId(UUID.randomUUID().toString().replace("_", ""));

            assertThrows(AgentStudioException.class, () -> {
                workSpaceMemberService.transferWorkspaceOwnership(projectId, body);
            });
            verify(workSpaceMemberMapper, never()).updateMemberRole(any());
        }
    }

    // 测试exitWorkspace方法
    @Test
    void testExitWorkspace_Success() {
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            mockRequestContextUtils(mocked);


            member.setRole(MemberRole.DEVELOPER.getValue());
            CommonResponse response = workSpaceMemberService.exitWorkspace(projectId, workspaceId);

            // 验证结果
            assertEquals(200, response.getCode());
            assertEquals(0, member.getStatus());
            assertEquals(memberId, member.getUpdaterId());
            verify(workSpaceMemberMapper, times(1))
                .deleteWorkspaceMemberByMemberIdWorkspaceId(member);
        }

    }

    @Test
    void testExitWorkspace_MemberNotExist() {
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            mockRequestContextUtils(mocked);
            when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(memberId, workspaceId))
                .thenReturn(null);
            assertThrows(AgentStudioException.class, () -> {
                workSpaceMemberService.exitWorkspace(projectId, workspaceId);
            });
            verify(workSpaceMemberMapper, never())
                .deleteWorkspaceMemberByMemberIdWorkspaceId(any());
        }
    }

    @Test
    void testExitWorkspace_OwnerCannotExit() {
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            mockRequestContextUtils(mocked);
            member.setRole(MemberRole.OWNER.getValue());

            // 执行并验证异常
            assertThrows(AgentStudioException.class, () -> {
                workSpaceMemberService.exitWorkspace(projectId, workspaceId);
            });
            verify(workSpaceMemberMapper, never())
                .deleteWorkspaceMemberByMemberIdWorkspaceId(any());
        }
    }
}
