package com.openjiuwen.studio.conversation.infrastructure.adapter;

import com.openjiuwen.studio.conversation.domain.model.ConversationMessage;
import com.openjiuwen.studio.conversation.domain.repository.ConversationRepository;

import okhttp3.Response;
import okhttp3.sse.EventSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 团队新协议监听器单元测试：只在完整输出边界落库（run_done → t_conversation_run、
 * sub_done → t_conversation_sub_run，按 ExecutionRef.subExecutionId 路由）；
 * 增量/边界事件（message/reasoning/user_message/run_start/sub_start/tool_*）仅透传不落库。
 */
class ConversationRunEventSourceListenerTest {

    private SseEmitter sseEmitter;
    private ConversationRepository conversationRepository;
    private ConversationRunEventSourceListener listener;

    @BeforeEach
    void setUp() {
        sseEmitter = mock(SseEmitter.class);
        conversationRepository = mock(ConversationRepository.class);
        listener = new ConversationRunEventSourceListener(sseEmitter, new CountDownLatch(1), "conv-1", "exec-1",
            "model-1", conversationRepository);
    }

    private void feedEvent(String data) {
        listener.onEvent(mock(EventSource.class), null, null, data);
    }

    @Test
    void testSubDoneAndRunDonePersisted_MessageReasoningNot() {
        feedEvent("{\"event\":\"user_message\",\"data\":{\"conversationId\":\"conv-1\",\"query\":\"hi\"},"
            + "\"executionId\":\"exec-1\",\"index\":0}");
        feedEvent("{\"event\":\"run_start\",\"data\":{},\"executionId\":\"exec-1\",\"index\":1}");
        feedEvent("{\"event\":\"message\",\"data\":{\"delta\":\"思考中\"},\"executionId\":\"exec-1\",\"index\":2}");
        feedEvent("{\"event\":\"reasoning\",\"data\":{\"content\":\"推理\"},\"executionId\":\"exec-1\",\"index\":3}");
        feedEvent("{\"event\":\"sub_start\",\"data\":{\"subExecutionId\":\"sub-1\",\"agentId\":\"agent-8daf\"},"
            + "\"executionId\":\"exec-1\",\"index\":4}");
        feedEvent("{\"event\":\"sub_done\",\"data\":{\"subExecutionId\":\"sub-1\",\"agentId\":\"agent-8daf\","
            + "\"text\":\"上海多云 18-26℃\"},\"executionId\":\"exec-1\",\"index\":5}");
        feedEvent("{\"event\":\"run_done\",\"data\":{\"text\":\"明天上海多云 18-26℃\"},\"executionId\":\"exec-1\","
            + "\"index\":6}");
        listener.onClosed(mock(EventSource.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ConversationMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(conversationRepository).appendMessages(eq("conv-1"), captor.capture());
        List<ConversationMessage> rows = captor.getValue();
        // 只有 sub_done + run_done 两条（user_message/run_start/message/reasoning/sub_start 不落库）
        assertEquals(2, rows.size());

        ConversationMessage run = rows.stream()
            .filter(r -> r.getExecutionRef().getSubExecutionId() == null)
            .findFirst().orElseThrow();
        assertEquals("assistant", run.getRole());
        assertEquals("明天上海多云 18-26℃", run.getContent());
        assertEquals("exec-1", run.getExecutionRef().getExecutionId());

        ConversationMessage sub = rows.stream()
            .filter(r -> r.getExecutionRef().getSubExecutionId() != null)
            .findFirst().orElseThrow();
        assertEquals("assistant", sub.getRole());
        assertEquals("上海多云 18-26℃", sub.getContent());
        assertEquals("sub-1", sub.getExecutionRef().getSubExecutionId());
        assertEquals("agent-8daf", sub.getExecutionRef().getAgentId());
        assertEquals("exec-1", sub.getExecutionRef().getExecutionId());
    }

    @Test
    void testOnlyIncrementalEvents_NoPersist() {
        feedEvent("{\"event\":\"user_message\",\"data\":{\"query\":\"hi\"},\"executionId\":\"exec-1\"}");
        feedEvent("{\"event\":\"message\",\"data\":{\"delta\":\"增量\"},\"executionId\":\"exec-1\"}");
        feedEvent("{\"event\":\"reasoning\",\"data\":{\"content\":\"推理\"},\"executionId\":\"exec-1\"}");
        listener.onClosed(mock(EventSource.class));
        // 无完整输出边界 → 不落库
        verify(conversationRepository, never()).appendMessages(anyString(), anyList());
    }
}
