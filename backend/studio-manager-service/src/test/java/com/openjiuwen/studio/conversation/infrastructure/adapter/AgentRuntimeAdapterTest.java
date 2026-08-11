package com.openjiuwen.studio.conversation.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.dto.agent.Message;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 团队对话直传路径（Phase 5）单元测试。
 *
 * <p>run() 不再预烘焙 IR：URL 直接构建为 /v1/inner/{project}/conversations/{conversation}/team，
 * 请求体直传 subAgentIds + modelDeploymentId + conversationHistory（无 systemPrompt、无 enable_history）。
 * 空 endpoint 时 URL 构建（Request.Builder.url）立即抛异常，不会进入真实网络，测试快速且确定性。</p>
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
        // 忠实模拟生产：${agent_runtime_endpoint:} → 空字符串，URL 无协议头 → OkHttp 抛 IllegalArgumentException
        ReflectionTestUtils.setField(adapter, "runtimeEndpoint", "");
    }

    @AfterEach
    void tearDown() {
        RequestContextUtils.remove();   // 清理 IAM 上下文，防串
    }

    /**
     * 团队端点 URL 形态（runtime /v1/inner 命名空间）：/v1/inner/{project}/conversations/{conversation}/team，
     * 不再是旧链 /v1/inner/.../agents/{agentId}/conversations/...。
     */
    @Test
    void testBuildTeamUrl_TeamEndpointShape() {
        Conversation conv = Conversation.builder()
                .conversationId("c1").projectId("p1").workspaceId("w1").build();
        assertEquals("/v1/inner/p1/conversations/c1/team?workspace_id=w1", adapter.buildTeamUrl(conv));
    }

    /**
     * 空 endpoint（无协议头）→ URL 构建抛 IllegalArgumentException（构建先于网络，测试确定性）。
     * 注意：okhttp 异常消息会截断 URL（如 "/v1/in..."），故 URL 形态由 testBuildTeamUrl 独立断言。
     */
    @Test
    void testRun_EmptyEndpoint_ThrowsIllegalArgumentException() {
        Conversation conv = Conversation.builder()
                .conversationId("c1").projectId("p1").workspaceId("w1").build();
        SendMessageCmd cmd = new SendMessageCmd();
        cmd.setQuery("hi");
        cmd.setModelDeploymentId("m1");

        assertThrows(IllegalArgumentException.class,
                () -> adapter.run(conv, cmd, List.of(), "exec-1", new HttpHeaders()));
    }

    /**
     * 不再预烘焙 IR（Phase 5）：run() 直传团队参数，ensureConversationIr/generateConversationIr/OBS 上传均不触发。
     */
    @Test
    void testRun_DoesNotGenerateIr() {
        Conversation conv = Conversation.builder()
                .conversationId("c1").projectId("p1").workspaceId("w1").build();
        SendMessageCmd cmd = new SendMessageCmd();
        cmd.setQuery("hi");
        cmd.setModelDeploymentId("m1");

        assertThrows(IllegalArgumentException.class,
                () -> adapter.run(conv, cmd, List.of(), "exec-1", new HttpHeaders()));

        verify(controllerManagementService, never()).generateConversationIr(anyList(), anyString(), anyString());
        verify(mgObsService, never()).uploadObsFile(any(), any(), any(), any(), any());
    }

    /**
     * 历史转换：平台 Message → 引擎契约 [{role, content}]（仅 role/content，避免跨服务反序列化类型坑）；
     * 空/null 返回 null（第一轮不注入）。
     */
    @Test
    void testToHistoryMaps_ConvertsMessagesToRoleContent() {
        List<Message> histories = List.of(
                new Message().setRole("user").setContent("上海的天气怎么样？"),
                new Message().setRole("assistant").setContent("上海多云 18-26℃"));
        List<Map<String, String>> maps = ReflectionTestUtils.invokeMethod(adapter, "toHistoryMaps", histories);
        assertNotNull(maps);
        assertEquals(2, maps.size());
        assertEquals("user", maps.get(0).get("role"));
        assertEquals("上海的天气怎么样？", maps.get(0).get("content"));
        assertEquals("assistant", maps.get(1).get("role"));

        assertNull(ReflectionTestUtils.invokeMethod(adapter, "toHistoryMaps", new Object[] { List.of() }));
        assertNull(ReflectionTestUtils.invokeMethod(adapter, "toHistoryMaps", new Object[] { null }));
    }

    /**
     * 回归：header 来自传入的 HttpHeaders（manager 统一模式）；X-Auth-Token 以 IAM 上下文为准补齐。
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
