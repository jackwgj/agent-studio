/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.aop;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.annotation.ValidPermission;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.iam.IamGroup;
import com.openjiuwen.studio.agent.manager.dto.iam.IamRole;
import com.openjiuwen.studio.agent.manager.dto.iam.IamUserGroupPermissionRsp;
import com.openjiuwen.studio.agent.manager.dto.iam.IamUserGroupRsp;
import com.openjiuwen.studio.agent.manager.entity.WorkSpaceMemberEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMemberMapper;
import com.openjiuwen.studio.agent.manager.service.PermissionService;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class CheckPermissionTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkspaceMemberMapper workSpaceMemberMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PermissionService permissionService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IamServiceUtils iamServiceUtils;

    @InjectMocks
    private CheckPermission checkPermission;

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
    void test_checkPermission_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils =
                mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId())
                    .thenReturn("not_empty");
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestProjectId())
                    .thenReturn("not_empty");
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestWorkspaceId())
                    .thenReturn("not_empty");
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken())
                    .thenReturn("not_empty");
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserName())
                    .thenReturn("not_empty");
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainName())
                    .thenReturn("not_empty");

                WorkSpaceMemberEntity workSpaceMemberEntity = new WorkSpaceMemberEntity();
                when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString()))
                    .thenReturn(workSpaceMemberEntity);

                List<String> list = new ArrayList<>();
                list.add("not_empty");
                when(permissionService.getMergedPermissions().get(null)).thenReturn(list);

                IamUserGroupRsp iamUserGroupRsp = new IamUserGroupRsp();
                List<IamGroup> groups = new ArrayList<>();
                IamGroup iamGroup = IamGroup.builder().build();
                groups.add(iamGroup);
                iamUserGroupRsp.setGroups(groups);
                when(iamServiceUtils.queryIamUserGroupList(anyString())).thenReturn(iamUserGroupRsp);
                IamUserGroupPermissionRsp iamUserGroupPermissionRsp = new IamUserGroupPermissionRsp();
                List<IamRole> roles = new ArrayList<>();
                IamRole iamRole = IamRole.builder().build();
                roles.add(iamRole);
                iamUserGroupPermissionRsp.setRoles(roles);
                when(iamServiceUtils.queryIamUserGroupPermissionList(anyString(), anyString()))
                    .thenReturn(iamUserGroupPermissionRsp);
                when(iamServiceUtils.queryDomainToken(anyString(), anyString())).thenReturn("not_empty");

                JoinPoint joinPoint = mock(JoinPoint.class, Answers.RETURNS_DEEP_STUBS);
                MethodSignature signature = mock(MethodSignature.class, Answers.RETURNS_DEEP_STUBS);
                ValidPermission validPermission = mock(ValidPermission.class, Answers.RETURNS_DEEP_STUBS);
                when(validPermission.value()).thenReturn("not_empty");
                when(signature.getMethod().getAnnotation(any())).thenReturn(validPermission);
                when(joinPoint.getSignature()).thenReturn(signature);

                // When
                checkPermission.checkPermission(joinPoint);
            }
        });
    }

    @Test
    void test_checkPermission_should_throw_exception() throws Exception {
        assertThrows(AgentStudioException.class, () -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils =
                mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId())
                    .thenReturn("not_empty");
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestProjectId())
                    .thenReturn("not_empty");
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestWorkspaceId())
                    .thenReturn("not_empty");
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken())
                    .thenReturn("not_empty");
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserName())
                    .thenReturn("not_empty");
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainName())
                    .thenReturn("not_empty");

                WorkSpaceMemberEntity workSpaceMemberEntity = new WorkSpaceMemberEntity();
                when(workSpaceMemberMapper.selectByMemberIdAndWorkspaceId(anyString(), anyString()))
                    .thenReturn(workSpaceMemberEntity);

                List<String> list = new ArrayList<>();
                list.add("not_empty");
                when(permissionService.getMergedPermissions().get(null)).thenReturn(list);

                IamUserGroupRsp iamUserGroupRsp = new IamUserGroupRsp();
                List<IamGroup> groups = new ArrayList<>();
                IamGroup iamGroup = IamGroup.builder().build();
                groups.add(iamGroup);
                iamUserGroupRsp.setGroups(groups);
                when(iamServiceUtils.queryIamUserGroupList(anyString())).thenReturn(iamUserGroupRsp);
                IamUserGroupPermissionRsp iamUserGroupPermissionRsp = new IamUserGroupPermissionRsp();
                List<IamRole> roles = new ArrayList<>();
                IamRole iamRole = IamRole.builder().build();
                roles.add(iamRole);
                iamUserGroupPermissionRsp.setRoles(roles);
                when(iamServiceUtils.queryIamUserGroupPermissionList(anyString(), anyString()))
                    .thenReturn(iamUserGroupPermissionRsp);
                when(iamServiceUtils.queryDomainToken(anyString(), anyString())).thenReturn("not_empty");

                JoinPoint joinPoint = mock(JoinPoint.class, Answers.RETURNS_DEEP_STUBS);
                MethodSignature signature = mock(MethodSignature.class, Answers.RETURNS_DEEP_STUBS);
                ValidPermission validPermission = mock(ValidPermission.class, Answers.RETURNS_DEEP_STUBS);
                when(validPermission.value()).thenReturn("empty");
                when(signature.getMethod().getAnnotation(any())).thenReturn(validPermission);
                when(joinPoint.getSignature()).thenReturn(signature);

                // When
                checkPermission.checkPermission(joinPoint);
            }
        });
    }

}
