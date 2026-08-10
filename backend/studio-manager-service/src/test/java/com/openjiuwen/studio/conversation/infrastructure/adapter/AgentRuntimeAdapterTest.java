package com.openjiuwen.studio.conversation.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.ControllerIR;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.ControllerManagementService;
import com.openjiuwen.studio.conversation.application.dto.SendMessageCmd;
import com.openjiuwen.studio.conversation.domain.model.Conversation;
import com.openjiuwen.studio.conversation.domain.repository.ConversationRepository;
import okhttp3.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 定位 AgentRuntimeAdapter 的配置依赖问题。
 *
 * <p>run() 中 URL 构建（Request.Builder.url）发生在 latch.await 之前，因此空 endpoint 会在这里
 * 立即抛异常，不会进入阻塞/真实网络，测试快速且确定性。</p>
 */
class AgentRuntimeAdapterTest {

    private ControllerManagementService controllerManagementService;
    private MgObsService mgObsService;
    private ConversationRepository conversationRepository;
    private OkHttpClientUtils okHttpClientUtils;
    private AgentRuntimeAdapter adapter;

    @BeforeEach
    void setUp() {
        controllerManagementService = mock(ControllerManagementService.class);
        mgObsService = mock(MgObsService.class);
        conversationRepository = mock(ConversationRepository.class);
        okHttpClientUtils = mock(OkHttpClientUtils.class);
        adapter = new AgentRuntimeAdapter(controllerManagementService, mgObsService,
                conversationRepository, okHttpClientUtils, new ObjectMapper());
        // @Value 字段在裸 new 下为 null，必须手工注入（Spring 只在 bean 创建时解析）。
        // 忠实模拟生产：${agent_runtime_endpoint:} → 空字符串、system-prompt → 非空默认值
        ReflectionTestUtils.setField(adapter, "runtimeEndpoint", "");
        ReflectionTestUtils.setField(adapter, "teamAgentIdsStr", "agent-team-1");
        ReflectionTestUtils.setField(adapter, "systemPrompt", "你是一个智能对话助手");
    }

    @AfterEach
    void tearDown() {
        RequestContextUtils.remove();   // 清理 IAM 上下文，防串
    }

    /**
     * 复现生产 bug：agent_runtime_endpoint 未配置（@Value 默认空字符串）→
     * url = "/v1/inner/..."（无协议头）→ OkHttp 抛 IllegalArgumentException。
     */
    @Test
    void testRun_EmptyRuntimeEndpoint_ThrowsIllegalArgumentException() {
        ControllerIR ir = new ControllerIR().setAgentId("agent-1").setMetadata(new HashMap<>());
        when(controllerManagementService.generateConversationIr(anyList(), anyString(), anyString()))
                .thenReturn(ir);
        Conversation conv = Conversation.builder()
                .conversationId("c1").projectId("p1").workspaceId("w1").build();
        SendMessageCmd cmd = new SendMessageCmd();
        cmd.setQuery("hi");
        cmd.setModelDeploymentId("m1");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adapter.run(conv, cmd, List.of(), "exec-1", new HttpHeaders()));

        // 异常信息与生产日志一致：URL 没有 scheme，指向 /v1/inner/...
        assertTrue(ex.getMessage().contains("Expected URL scheme"));
        assertTrue(ex.getMessage().contains("/v1/in"));
    }

    /**
     * 相关配置依赖：conversation-workspace.team-agent-ids 未配置 →
     * ensureConversationIr 在 URL 构建之前抛 IllegalStateException。
     */
    @Test
    void testRun_EmptyTeamAgentIds_ThrowsIllegalStateException() {
        ReflectionTestUtils.setField(adapter, "teamAgentIdsStr", "");
        Conversation conv = Conversation.builder()
                .conversationId("c1").projectId("p1").workspaceId("w1").build();
        SendMessageCmd cmd = new SendMessageCmd();
        cmd.setQuery("hi");
        cmd.setModelDeploymentId("m1");

        assertThrows(IllegalStateException.class,
                () -> adapter.run(conv, cmd, List.of(), "exec-1", new HttpHeaders()));
    }

    /**
     * 回归：header 来自传入的 HttpHeaders（manager 统一模式），不再读 manager 里没人填充的
     * RequestContextUtils.getHeaders()；X-Auth-Token 以 IAM 上下文为准补齐，供 runtime POC 认证。
     */
    @Test
    void testCopyRequestHeaders_AddsRequestHeadersAndAuthTokenFromIamContext() {
        RequestContextUtils.setRequestAuthTokenAndProjectId("u1|p1", "p1");
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Language", "zh-cn");
        headers.add("stream", "true");
        headers.add("X-Auth-Token", "external-token");   // 外部传入的 X-Auth-Token 应被忽略

        Request.Builder builder = new Request.Builder().url("http://runtime:31014/x");
        adapter.copyRequestHeaders(builder, headers);

        Request request = builder.build();
        assertEquals("zh-cn", request.header("X-Language"));
        assertEquals("true", request.header("stream"));
        // X-Auth-Token 取自 IAM 上下文，未重复添加外部值
        assertEquals("u1|p1", request.header("X-Auth-Token"));
    }
}
