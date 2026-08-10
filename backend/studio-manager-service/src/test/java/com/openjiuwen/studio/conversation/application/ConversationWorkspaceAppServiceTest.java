package com.openjiuwen.studio.conversation.application;

import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.connection.model.PageResult;
import com.openjiuwen.studio.conversation.application.dto.ConversationCreateCmd;
import com.openjiuwen.studio.conversation.application.dto.ConversationDetailVo;
import com.openjiuwen.studio.conversation.application.dto.ConversationListQuery;
import com.openjiuwen.studio.conversation.application.dto.ConversationVo;
import com.openjiuwen.studio.conversation.application.dto.MessageVo;
import com.openjiuwen.studio.conversation.application.dto.SendMessageCmd;
import com.openjiuwen.studio.conversation.domain.model.Conversation;
import com.openjiuwen.studio.conversation.domain.model.ConversationMessage;
import com.openjiuwen.studio.conversation.domain.repository.ConversationRepository;
import com.openjiuwen.studio.conversation.domain.service.ConversationHistoryService;
import com.openjiuwen.studio.conversation.infrastructure.adapter.AgentRuntimeAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConversationWorkspaceAppServiceTest {

    private ConversationRepository repository;
    private ConversationHistoryService historyService;
    private AgentRuntimeAdapter runtimeAdapter;
    private ConversationWorkspaceAppService appService;

    @BeforeEach
    void setUp() {
        repository = mock(ConversationRepository.class);
        historyService = mock(ConversationHistoryService.class);
        runtimeAdapter = mock(AgentRuntimeAdapter.class);
        appService = new ConversationWorkspaceAppService(repository, historyService, runtimeAdapter);

        SimpleUser user = new SimpleUser();
        user.setUserId("u1");
        user.setDomainId("d1");
        RequestContextUtils.setContext(user);
    }

    @AfterEach
    void tearDown() {
        RequestContextUtils.remove();   // 清理 ThreadLocal，防串
    }

    // ---------- create ----------

    @Test
    void testCreate_BlankTitle_UseDefaultTitle() {
        ConversationCreateCmd cmd = new ConversationCreateCmd();
        // 不 setTitle，保持 null
        ConversationVo vo = appService.create("p1", "w1", cmd);
        assertEquals("新会话", vo.getTitle());
        verify(repository).save(any());   // 断言确实保存了
    }

    @Test
    void testCreate_WithTitle_UseGivenTitle() {
        ConversationCreateCmd cmd = new ConversationCreateCmd();
        cmd.setTitle("自定义标题");
        cmd.setSource("source1");
        ConversationVo vo = appService.create("p1", "w1", cmd);
        assertEquals("自定义标题", vo.getTitle());
        assertEquals("source1", vo.getSource());
        verify(repository).save(any());   // 断言确实保存了
    }

    // ---------- list ----------

    @Test
    void testList_MapsRepositoryToVos() {
        when(repository.countByOwner("p1", "w1", "u1")).thenReturn(3L);
        when(repository.listByOwner("p1", "w1", "u1", 0, 20)).thenReturn(List.of(
                ownedConversation("c1"),
                ownedConversation("c2")));

        PageResult<ConversationVo> result = appService.list("p1", "w1", new ConversationListQuery());

        assertEquals(3L, result.getTotalCount());
        assertEquals(2, result.getItems().size());
        assertEquals("c1", result.getItems().get(0).getConversationId());
        assertEquals("c2", result.getItems().get(1).getConversationId());
    }

    @Test
    void testList_WithCustomPageSize() {
        when(repository.countByOwner("p1", "w1", "u1")).thenReturn(0L);
        when(repository.listByOwner("p1", "w1", "u1", 2, 50)).thenReturn(List.of());

        ConversationListQuery query = new ConversationListQuery();
        query.setPage(2);
        query.setSize(50);
        PageResult<ConversationVo> result = appService.list("p1", "w1", query);

        assertEquals(0L, result.getTotalCount());
        assertTrue(result.getItems().isEmpty());
        verify(repository).listByOwner("p1", "w1", "u1", 2, 50);
    }

    // ---------- detail ----------

    @Test
    void testDetail_ConversationNotFound_Throws() {
        when(repository.findById("c1")).thenReturn(Optional.empty());

        assertThrows(AgentStudioException.class, () -> appService.detail("p1", "w1", "c1"));
    }

    @Test
    void testDetail_NotOwned_Throws() {
        Conversation other = ownedConversation("c1");
        other.setOwnerUserId("other-user");
        when(repository.findById("c1")).thenReturn(Optional.of(other));

        assertThrows(AgentStudioException.class, () -> appService.detail("p1", "w1", "c1"));
    }

    @Test
    void testDetail_Owned_ReturnsMessages() {
        Conversation conv = ownedConversation("c1");
        conv.setMessages(List.of(
                ConversationMessage.builder().role("user").content("hi").build()));
        when(repository.findById("c1")).thenReturn(Optional.of(conv));

        ConversationDetailVo vo = appService.detail("p1", "w1", "c1");

        assertEquals("c1", vo.getConversationId());
        assertEquals(1, vo.getMessages().size());
        assertEquals("user", vo.getMessages().get(0).getRole());
    }

    @Test
    void testDetail_MessageWithoutRefs_MapsNullFields() {
        Conversation conv = ownedConversation("c1");
        conv.setMessages(List.of(
                ConversationMessage.builder().role("user").content("hi").build()));
        when(repository.findById("c1")).thenReturn(Optional.of(conv));

        ConversationDetailVo vo = appService.detail("p1", "w1", "c1");

        MessageVo m = vo.getMessages().get(0);
        assertEquals("user", m.getRole());
        // 引用为 null 时，映射不抛异常且字段为 null
        assertNull(m.getToolId());
        assertNull(m.getToolArgs());
        assertNull(m.getFileIds());
        assertNull(m.getExecutionId());
        assertNull(m.getSubExecutionId());
        assertNull(m.getAgentId());
    }

    // ---------- delete ----------

    @Test
    void testDelete_Owned_CallsSoftDelete() {
        when(repository.findById("c1")).thenReturn(Optional.of(ownedConversation("c1")));

        appService.delete("p1", "w1", "c1");

        verify(repository).softDelete("c1");
    }

    @Test
    void testDelete_NotOwned_Throws() {
        Conversation other = ownedConversation("c1");
        other.setOwnerUserId("other-user");
        when(repository.findById("c1")).thenReturn(Optional.of(other));

        assertThrows(AgentStudioException.class, () -> appService.delete("p1", "w1", "c1"));
        verify(repository, never()).softDelete("c1");   // 未授权：不允许软删
    }

    // ---------- sendMessage ----------

    @Test
    void testSendMessage_BlankQuery_Throws() {
        SendMessageCmd cmd = new SendMessageCmd();

        assertThrows(AgentStudioException.class,
                () -> appService.sendMessage("p1", "w1", "c1", cmd, new HttpHeaders()));
        verify(repository, never()).appendMessages(anyString(), anyList());
    }

    @Test
    void testSendMessage_BlankModelDeploymentId_Throws() {
        SendMessageCmd cmd = new SendMessageCmd();
        cmd.setQuery("hi");

        assertThrows(AgentStudioException.class,
                () -> appService.sendMessage("p1", "w1", "c1", cmd, new HttpHeaders()));
        verify(repository, never()).appendMessages(anyString(), anyList());
    }

    @Test
    void testSendMessage_HappyPath_AppendsUserMessageAndRuns() {
        Conversation conv = ownedConversation("c1");
        when(repository.findById("c1")).thenReturn(Optional.of(conv));
        SendMessageCmd cmd = new SendMessageCmd();
        cmd.setQuery("你好");
        cmd.setModelDeploymentId("m1");
        SseEmitter emitter = new SseEmitter();
        when(runtimeAdapter.run(eq(conv), eq(cmd), anyList(), anyString(), any())).thenReturn(emitter);

        SseEmitter result = appService.sendMessage("p1", "w1", "c1", cmd, new HttpHeaders());

        assertSame(emitter, result);
        // ArgumentCaptor：不止验证"调用了 appendMessages"，还抓住传进去的消息内容深入断言
        ArgumentCaptor<List<ConversationMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).appendMessages(eq("c1"), captor.capture());
        List<ConversationMessage> appended = captor.getValue();
        assertEquals(1, appended.size());
        assertEquals("user", appended.get(0).getRole());
        assertEquals("你好", appended.get(0).getContent());
        verify(historyService).assemble(any());
        verify(runtimeAdapter).run(eq(conv), eq(cmd), anyList(), anyString(), any());
    }

    @Test
    void testSendMessage_ConversationNotFound_Throws() {
        when(repository.findById("c1")).thenReturn(Optional.empty());
        SendMessageCmd cmd = new SendMessageCmd();
        cmd.setQuery("hi");
        cmd.setModelDeploymentId("m1");

        assertThrows(AgentStudioException.class,
                () -> appService.sendMessage("p1", "w1", "c1", cmd, new HttpHeaders()));
        verify(repository, never()).appendMessages(anyString(), anyList());
    }

    // ---------- 工具方法 ----------

    private Conversation ownedConversation(String conversationId) {
        return Conversation.builder()
                .conversationId(conversationId)
                .title("会话")
                .projectId("p1")
                .workspaceId("w1")
                .ownerUserId("u1")
                .status(ConversationWorkspaceAppService.STATUS_ACTIVE)
                .build();
    }
}
