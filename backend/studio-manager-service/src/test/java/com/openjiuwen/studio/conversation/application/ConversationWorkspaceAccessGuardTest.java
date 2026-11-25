package com.openjiuwen.studio.conversation.application;

import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.entity.WorkSpaceMemberEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMapper;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMemberMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConversationWorkspaceAccessGuardTest {
    private WorkspaceMapper workspaceMapper;
    private WorkspaceMemberMapper memberMapper;
    private ConversationWorkspaceAccessGuard guard;

    @BeforeEach
    void setUp() {
        workspaceMapper = mock(WorkspaceMapper.class);
        memberMapper = mock(WorkspaceMemberMapper.class);
        guard = new ConversationWorkspaceAccessGuard(workspaceMapper, memberMapper);
        SimpleUser user = new SimpleUser();
        user.setProjectId("project-a");
        user.setUserId("user-a");
        RequestContextUtils.setContext(user);
    }

    @AfterEach
    void tearDown() {
        RequestContextUtils.remove();
    }

    @Test
    void conversationBusinessGuardRejectsAnotherProject() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> guard.requireAccess("project-b", "workspace-a"));
        assertEquals(StudioError.USER_WORKSPACE_PERMISSION_INVALID, exception.getErrorCode());
        verifyNoInteractions(workspaceMapper, memberMapper);
    }

    @Test
    void conversationBusinessGuardRejectsUnknownWorkspace() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> guard.requireAccess("project-a", "workspace-a"));
        assertEquals(StudioError.WORKSPACE_ID_INVALID, exception.getErrorCode());
        verifyNoInteractions(memberMapper);
    }

    @Test
    void conversationBusinessGuardRejectsNonMembers() {
        when(workspaceMapper.getWorkspaceByWorkspaceId("project-a", "workspace-a"))
            .thenReturn(new WorkspaceEntity());
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> guard.requireAccess("project-a", "workspace-a"));
        assertEquals(StudioError.USER_WORKSPACE_PERMISSION_INVALID, exception.getErrorCode());
    }

    @Test
    void conversationBusinessGuardAcceptsCurrentProjectMember() {
        when(workspaceMapper.getWorkspaceByWorkspaceId("project-a", "workspace-a"))
            .thenReturn(new WorkspaceEntity());
        when(memberMapper.selectByMemberIdAndWorkspaceId("user-a", "workspace-a"))
            .thenReturn(new WorkSpaceMemberEntity());
        assertDoesNotThrow(() -> guard.requireAccess("project-a", "workspace-a"));
    }
}
