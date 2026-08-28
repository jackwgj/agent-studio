package com.openjiuwen.studio.conversation.infrastructure.adapter;

import com.openjiuwen.studio.conversation.domain.model.ConversationMessage;
import com.openjiuwen.studio.conversation.domain.model.ConversationWorkflowNode;
import com.openjiuwen.studio.conversation.domain.repository.ConversationRepository;

import okhttp3.Response;
import okhttp3.sse.EventSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** Canonical conversation events must survive Manager persistence and history reload. */
class ConversationRunEventSourceListenerTest {
    private static final String EXECUTION_ID = "execution-1";
    private static final String ROOT_RUN_ID = "run-root";
    private static final String CHILD_RUN_ID = "run-child";

    private SseEmitter emitter;
    private ConversationRepository repository;
    private ConversationRunEventSourceListener listener;

    @BeforeEach
    void setUp() {
        emitter = mock(SseEmitter.class);
        repository = mock(ConversationRepository.class);
        listener = new ConversationRunEventSourceListener(emitter, new CountDownLatch(1), "conversation-1",
            EXECUTION_ID, "model-1", repository);
    }

    @Test
    void canonicalRunTreeMessagesAndTerminalEventsArePersistedWithTheirParentage() {
        feed(canonical("run_start", ROOT_RUN_ID, null, "agent", "{\"status\":\"running\"}"));
        feed(canonical("message", ROOT_RUN_ID, null, "agent", "{\"delta\":\"主回答\",\"agentId\":\"supervisor\"}"));
        feed(canonical("run_start", CHILD_RUN_ID, ROOT_RUN_ID, "agent", "{\"status\":\"running\",\"agentId\":\"agent-a\"}"));
        feed(canonical("reasoning", CHILD_RUN_ID, ROOT_RUN_ID, "agent", "{\"content\":\"子思考\",\"agentId\":\"agent-a\"}"));
        feed(canonical("message", CHILD_RUN_ID, ROOT_RUN_ID, "agent", "{\"delta\":\"子回答\",\"agentId\":\"agent-a\"}"));
        feed(canonical("run_end", CHILD_RUN_ID, ROOT_RUN_ID, "agent", "{\"status\":\"success\"}"));
        feed(canonical("run_end", ROOT_RUN_ID, null, "agent", "{\"status\":\"success\"}"));
        listener.onClosed(mock(EventSource.class));

        List<ConversationMessage> rows = captureMessages();
        assertEquals(List.of("run_start", "message", "run_start", "reasoning", "message", "run_end", "run_end"),
            rows.stream().map(ConversationMessage::getEvent).toList());
        ConversationMessage childMessage = rows.stream()
            .filter(row -> "message".equals(row.getEvent()) && CHILD_RUN_ID.equals(row.getExecutionRef().getRunId()))
            .findFirst().orElseThrow();
        assertEquals(ROOT_RUN_ID, childMessage.getExecutionRef().getParentRunId());
        assertEquals("agent-a", childMessage.getExecutionRef().getAgentId());
        assertEquals("agent", childMessage.getExecutionRef().getExecutionType());
        assertEquals("子回答", childMessage.getContent());
        assertTrue(rows.stream().map(ConversationMessage::getCreatedAt).allMatch(java.util.Objects::nonNull));
    }

    @Test
    void skillAndErrorEventsArePersistedForHistoryRecovery() {
        feed(canonical("skill_activated", CHILD_RUN_ID, ROOT_RUN_ID, "agent",
            "{\"skillId\":\"skill-1\",\"name\":\"会议纪要\",\"versionId\":\"version-1\"}"));
        feed(canonical("error", CHILD_RUN_ID, ROOT_RUN_ID, "agent",
            "{\"code\":\"tool_failed\",\"message\":\"boom\"}"));
        listener.onClosed(mock(EventSource.class));

        List<ConversationMessage> rows = captureMessages();
        assertEquals(List.of("skill_activated", "error"), rows.stream().map(ConversationMessage::getEvent).toList());
        assertTrue(rows.get(0).getContent().contains("会议纪要"));
        assertTrue(rows.get(1).getContent().contains("tool_failed"));
        rows.forEach(row -> {
            assertEquals(CHILD_RUN_ID, row.getExecutionRef().getRunId());
            assertEquals(ROOT_RUN_ID, row.getExecutionRef().getParentRunId());
        });
    }

    @Test
    void toolResultAndWorkflowNodeRetainCanonicalIdentifiers() {
        feed(canonical("tool_call", CHILD_RUN_ID, ROOT_RUN_ID, "agent",
            "{\"toolId\":\"call-1\",\"toolName\":\"search\",\"arguments\":{\"q\":\"上海\"},\"agentId\":\"agent-a\"}"));
        feed(canonical("tool_result", CHILD_RUN_ID, ROOT_RUN_ID, "agent",
            "{\"toolId\":\"call-1\",\"toolName\":\"search\",\"result\":\"晴\",\"agentId\":\"agent-a\"}"));
        feed(canonical("workflow_node", "workflow-run", ROOT_RUN_ID, "workflow",
            "{\"toolId\":\"workflow-call\",\"workflowId\":\"workflow-1\",\"nodeId\":\"node-1\","
                + "\"nodeName\":\"查询\",\"nodeType\":\"tool\",\"nodeIndex\":1,\"status\":\"completed\","
                + "\"input\":{\"city\":\"上海\"},\"output\":{\"weather\":\"晴\"}}"));
        listener.onClosed(mock(EventSource.class));

        List<ConversationMessage> rows = captureMessages();
        ConversationMessage tool = rows.stream().filter(row -> "tool_result".equals(row.getEvent()))
            .findFirst().orElseThrow();
        assertEquals("call-1", tool.getToolRef().getToolId());
        assertEquals("search", tool.getToolRef().getToolName());
        assertTrue(tool.getToolRef().getArgs().contains("上海"));
        assertEquals(CHILD_RUN_ID, tool.getExecutionRef().getRunId());

        ConversationMessage workflowEvent = rows.stream().filter(row -> "workflow_node".equals(row.getEvent()))
            .findFirst().orElseThrow();
        assertEquals("workflow-run", workflowEvent.getExecutionRef().getRunId());
        assertEquals("workflow", workflowEvent.getExecutionRef().getExecutionType());
        assertEquals("workflow-1", workflowEvent.getWorkflowId());
        assertEquals("node-1", workflowEvent.getNodeId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ConversationWorkflowNode>> workflowCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).appendWorkflowNodes(eq("conversation-1"), workflowCaptor.capture());
        ConversationWorkflowNode node = workflowCaptor.getValue().get(0);
        assertEquals("workflow-call", node.getToolId());
        assertEquals(ROOT_RUN_ID, node.getParentRunId());
        assertEquals("查询", node.getNodeName());
        assertTrue(node.getInputContent().contains("上海"));
    }

    @Test
    void artifactPersistsDurableMetadataCanonicalRunAndTrustedExecutionOwnership() {
        String objectKey = "conversation-artifacts/project/workspace/user/conversation/execution/report.pdf";
        String checksum = "0".repeat(64);
        feed(canonical("artifact", ROOT_RUN_ID, null, "agent", "{\"executionId\":\"" + EXECUTION_ID
            + "\",\"objectKey\":\"" + objectKey + "\",\"fileName\":\"测试报告.pdf\",\"size\":128,"
            + "\"mediaType\":\"application/pdf\",\"checksum\":\"" + checksum + "\"}"));
        listener.onClosed(mock(EventSource.class));

        ConversationMessage artifact = captureMessages().get(0);
        assertEquals("artifact", artifact.getEvent());
        assertEquals(ROOT_RUN_ID, artifact.getExecutionRef().getRunId());
        assertNull(artifact.getExecutionRef().getParentRunId());
        assertEquals(1, artifact.getFileRefs().size());
        assertEquals(objectKey, artifact.getFileRefs().get(0).getObjectKey());
        assertEquals("测试报告.pdf", artifact.getFileRefs().get(0).getFileName());
        assertEquals(128L, artifact.getFileRefs().get(0).getSize());
        assertEquals("application/pdf", artifact.getFileRefs().get(0).getMediaType());
        assertEquals(checksum, artifact.getFileRefs().get(0).getChecksum());
        assertEquals(EXECUTION_ID, artifact.getFileRefs().get(0).getExecutionId());
    }

    @Test
    void artifactWithDifferentExecutionOwnershipIsRejected() {
        feed(canonical("artifact", ROOT_RUN_ID, null, "agent", "{\"executionId\":\"other-execution\","
            + "\"objectKey\":\"conversation-artifacts/p/w/u/c/e/a.txt\",\"fileName\":\"a.txt\",\"size\":1,"
            + "\"mediaType\":\"text/plain\",\"checksum\":\"" + "0".repeat(64) + "\"}"));
        listener.onClosed(mock(EventSource.class));

        verify(repository, never()).appendMessages(anyString(), anyList());
    }

    @Test
    void closePersistsBeforeBrowserDoneMarkerAndFlushIsIdempotent() throws Exception {
        feed(canonical("message", ROOT_RUN_ID, null, "agent", "{\"delta\":\"最终回答\"}"));
        feed(canonical("run_end", ROOT_RUN_ID, null, "agent", "{\"status\":\"success\"}"));
        EventSource source = mock(EventSource.class);
        listener.onClosed(source);
        listener.onFailure(source, new RuntimeException("late failure"), mock(Response.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ConversationMessage>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<ResponseBodyEmitter.DataWithMediaType>> doneCaptor = ArgumentCaptor.forClass(Set.class);
        InOrder order = inOrder(repository, emitter);
        order.verify(repository).appendMessages(eq("conversation-1"), rowsCaptor.capture());
        order.verify(emitter).send(doneCaptor.capture());
        order.verify(emitter).complete();
        verify(repository, times(1)).appendMessages(eq("conversation-1"), anyList());
        String payload = doneCaptor.getValue().stream().map(item -> item.getData().toString())
            .collect(Collectors.joining());
        assertEquals("data:[DONE]\n\n", payload);
    }

    @SuppressWarnings("unchecked")
    private List<ConversationMessage> captureMessages() {
        ArgumentCaptor<List<ConversationMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).appendMessages(eq("conversation-1"), captor.capture());
        return captor.getValue();
    }

    private void feed(String event) {
        listener.onEvent(mock(EventSource.class), null, null, event);
    }

    private static String canonical(String event, String runId, String parentRunId, String executionType,
                                    String dataFields) {
        String parent = parentRunId == null ? "null" : "\"" + parentRunId + "\"";
        String mergedData = dataFields.substring(0, dataFields.length() - 1)
            + ",\"runId\":\"" + runId + "\",\"parentRunId\":" + parent
            + ",\"executionType\":\"" + executionType + "\"}";
        return "{\"event\":\"" + event + "\",\"conversationId\":\"conversation-1\","
            + "\"runId\":\"" + runId + "\",\"parentRunId\":" + parent
            + ",\"executionType\":\"" + executionType + "\",\"index\":1,\"data\":" + mergedData + "}";
    }
}
