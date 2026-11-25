package com.openjiuwen.studio.agent.manager.filter;

import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.ThreadLocalUtils;
import com.openjiuwen.studio.agent.manager.entity.WorkSpaceMemberEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMapper;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMemberMapper;
import com.openjiuwen.studio.agent.manager.service.PermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceInterceptorTest {
    @Mock
    private WorkspaceMapper workspaceMapper;
    @Mock
    private WorkspaceMemberMapper workspaceMemberMapper;
    @Mock
    private PermissionService permissionService;
    @InjectMocks
    private WorkspaceInterceptor interceptor;

    @BeforeEach
    void setUp() {
        SimpleUser user = new SimpleUser();
        user.setUserId("user-a");
        user.setProjectId("project-a");
        RequestContextUtils.setContext(user);
    }

    @AfterEach
    void tearDown() {
        RequestContextUtils.remove();
        ThreadLocalUtils.clearWorkspaceId();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/v1/project-a/conversation/sessions",
        "/v1/project-a/conversation/sessions/conversation-a/messages",
        "/v1/project-a/conversation/sessions/input-files",
        "/v1/project-a/conversation/sessions/skills",
        "/v1/studio/datasources/source-a/execute"
    })
    void conversationAndOfficialDatasourceRoutesRetainTheirWorkspaceExemptions(String uri) throws Exception {
        assertTrue(interceptor.preHandle(request(uri, null), new MockHttpServletResponse(), new Object()));
        verifyNoInteractions(workspaceMapper, workspaceMemberMapper, permissionService);
    }

    @Test
    void deprecatedMaasEndpointIsNoLongerExempt() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> interceptor.preHandle(request("/v1/project-a/model-manager/maas-model-services", null),
                new MockHttpServletResponse(), new Object()));
        assertEquals(StudioError.WORKSPACE_FORMAT_INVALID, exception.getErrorCode());
        verifyNoInteractions(workspaceMapper, workspaceMemberMapper, permissionService);
    }

    @Test
    void conversationExemptionDoesNotIncludeOtherConversationRoutes() {
        assertThrows(AgentStudioException.class,
            () -> interceptor.preHandle(request("/v1/project-a/conversation/admin", null),
                new MockHttpServletResponse(), new Object()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "bad/id", "invalid.workspace"})
    void invalidWorkspaceIdsAreRejectedBeforeDatabaseAccess(String workspaceId) {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> interceptor.preHandle(request("/v1/project-a/agent-manager/agents", workspaceId),
                new MockHttpServletResponse(), new Object()));
        assertEquals(StudioError.WORKSPACE_FORMAT_INVALID, exception.getErrorCode());
        verifyNoInteractions(workspaceMapper, workspaceMemberMapper, permissionService);
    }

    @Test
    void workspaceIdLongerThan64CharactersIsRejected() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> interceptor.preHandle(request("/v1/project-a/agent-manager/agents", "w".repeat(65)),
                new MockHttpServletResponse(), new Object()));
        assertEquals(StudioError.WORKSPACE_FORMAT_INVALID, exception.getErrorCode());
        verifyNoInteractions(workspaceMapper, workspaceMemberMapper, permissionService);
    }

    @Test
    void normalRoutesStillRequireWorkspaceMembership() {
        when(workspaceMapper.getWorkspaceByWorkspaceId("project-a", "workspace-a"))
            .thenReturn(new WorkspaceEntity());
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> interceptor.preHandle(request("/v1/project-a/agent-manager/agents", "workspace-a"),
                new MockHttpServletResponse(), new Object()));
        assertEquals(StudioError.USER_WORKSPACE_PERMISSION_INVALID, exception.getErrorCode());
        verify(workspaceMemberMapper).selectByMemberIdAndWorkspaceId("user-a", "workspace-a");
        verifyNoInteractions(permissionService);
    }

    @Test
    void normalRoutesStillEnforceRegisteredRolePermissions() {
        when(workspaceMapper.getWorkspaceByWorkspaceId("project-a", "workspace-a"))
            .thenReturn(new WorkspaceEntity());
        when(workspaceMemberMapper.selectByMemberIdAndWorkspaceId("user-a", "workspace-a"))
            .thenReturn(new WorkSpaceMemberEntity().setRole("reader"));
        when(permissionService.getMergedPermissions()).thenReturn(Map.of(
            "REGISTERED_URIS", List.of("POST#/v1/{project_id}/agent-manager/agents"),
            "reader", List.of()));

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> interceptor.preHandle(request("/v1/project-a/agent-manager/agents", "workspace-a"),
                new MockHttpServletResponse(), new Object()));
        assertEquals(StudioError.USER_NO_PERMISSION_DO_THIS, exception.getErrorCode());
    }

    private MockHttpServletRequest request(String uri, String workspaceId) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("project_id", "project-a"));
        if (workspaceId != null) {
            request.setParameter("workspace_id", workspaceId);
        }
        return request;
    }
}
