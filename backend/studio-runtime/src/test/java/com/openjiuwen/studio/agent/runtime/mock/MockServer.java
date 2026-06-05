/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.Conversation.TEST_MULTI_AGENT_CONVERSATION_ID;

import com.alibaba.fastjson.JSON;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.openjiuwen.studio.agent.runtime.constant.ConstantTest;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenDeleteIrRsp;
import com.openjiuwen.studio.agent.runtime.utils.TestUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockServer {
    private static final int PORT = 10001;

    private static WireMockServer wireMockServer;

    public static void init() {
        initWorkflowServer();
        initAgentServer();
    }

    private static void start() {
        if (wireMockServer != null) {
            return;
        }
        wireMockServer = new WireMockServer(options().port(PORT));
        wireMockServer.start();
    }

    private static void initWorkflowServer() {
        start();
        mockJiuwenWfStream();
        mockJiuwenInsightWfStream();
        mockDeleteConversation();
        mockJiuwenWfStreamFailed();
    }

    private static void initAgentServer() {
        start();
        mockChat(containing("agent"), "classpath:response/additional_questions_result.json");
        mockChat(containing("exception"), "classpath:response/additional_questions_result_error.txt");
        mockPromptBuilder();
        mockJiuwenAgentStream();
        mockJiuwenAgentWithWorkflowStream();
        mockJiuwenTwqWfStream();
        mockJiuwenAgentWithMultiAgentStream();
        mockModelAvailableQuery();
        mockChat(containing("信息总结助手"), "classpath:response/generate_memory_chunk_result.json");
    }

    private static void mockChat(StringValuePattern pattern, String resourceLocation) {
        wireMockServer.stubFor(
            post(urlEqualTo("/v1/chat/completions")).withRequestBody(matchingJsonPath("$.messages[0].content", pattern))
                .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }

    private static void mockJiuwenWfStream() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/orchestration/ir/execute")).withRequestBody(
                matchingJsonPath("$.conversationId", equalTo("test-user-conversation-123")))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(TestUtil.getStringFromFile("classpath:response/jiuwen_workflow_execute_stream.txt"))));
    }

    private static void mockJiuwenWfStreamFailed() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/orchestration/ir/execute")).withRequestBody(
                matchingJsonPath("$.conversationId", equalTo("test-user-conversation-failed")))
            .willReturn(aResponse().withStatus(500)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(TestUtil.getStringFromFile("classpath:response/jiuwen_workflow_failed.json"))));
    }

    private static void mockJiuwenTwqWfStream() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/orchestration/ir/execute")).withRequestBody(
                matchingJsonPath("$.conversationId", equalTo(ConstantTest.Conversation.TEST_TWQ_CONVERSATION_ID)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(TestUtil.getStringFromFile("classpath:response/jiuwen_workflow_twq_stream.txt"))));
    }

    private static void mockJiuwenInsightWfStream() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/orchestration/ir/execute")).withRequestBody(
                matchingJsonPath("$.conversationId", equalTo("test-user-conversation-insight-123")))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(TestUtil.getStringFromFile("classpath:response/jiuwen_workflow_insight_stream.txt"))));
    }

    private static void mockPromptBuilder() {
        wireMockServer.stubFor(
            post(urlEqualTo("/v1/prompt/build")).withRequestBody(matchingJsonPath("$.instruct", equalTo("旅游助手")))
                .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "text/event-stream")
                    .withBody(TestUtil.getStringFromFile("classpath:response/prompt_builder.txt"))));
    }

    private static void mockDeleteConversation() {
        wireMockServer.stubFor(delete(urlEqualTo("/v1/orchestration/ir/execute")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(JSON.toJSONString(JiuWenDeleteIrRsp.builder().code(0).message("ok").build()))));
    }

    private static void mockJiuwenAgentStream() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/orchestration/ir/execute")).withRequestBody(
                matchingJsonPath("$.conversationId", equalTo("test-run-agent-conversation-id")))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(TestUtil.getStringFromFile("classpath:response/jiuwen_agent_execute_stream.txt"))));
    }

    private static void mockJiuwenAgentWithWorkflowStream() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/orchestration/ir/execute")).withRequestBody(
                matchingJsonPath("$.conversationId", equalTo("test-run-agent-with-workflow-conversation-id")))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(
                    TestUtil.getStringFromFile("classpath:response/jiuwen_agent_with_workflow_execute_stream.txt"))));
    }

    private static void mockJiuwenAgentWithMultiAgentStream() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/orchestration/ir/execute")).withRequestBody(
                matchingJsonPath("$.conversationId", equalTo(TEST_MULTI_AGENT_CONVERSATION_ID)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(TestUtil.getStringFromFile("classpath:response/jiuwen_agent_with_multi_agent_stream.txt"))));
    }

    private static void mockJiuwenWorkflowComponentExecute() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/orchestration/component/execute")).withRequestBody(
                matchingJsonPath("$.conversationId", equalTo("test-user-conversation-123")))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(TestUtil.getStringFromFile("classpath:response/jiuwen_workflow_component_execute.txt"))));
    }

    private static void mockModelAvailableQuery() {
        wireMockServer.stubFor(
            get(urlPathTemplate("/v1/{project_id}/model-manager/available/model-services")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"total\":1}")));
    }
}
