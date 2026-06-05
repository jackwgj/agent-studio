/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.utils.TestUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockServer {
    private static final int PORT = 10002;

    private static WireMockServer wireMockServer;

    public static void init() {
        // 如果本地有真实环境的配置文件，则使用真实地址请求，不使用mock
        if (MockServer.class.getClassLoader().getResource("llm-real.properties") != null) {
            return;
        }
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

        // workflow 组件测试
        mockPythonSandbox(
            containing("\ndef get_result():\n    return sympy.solve(['x+y-(51)', '2*x+4*y-(172)'], ['x', 'y'])\n"),
            containing(Constants.TEST_WORKFLOW_EXECUTION_ID), "classpath:response/python_interpreter_test.json");
        mockWebSearch(containing("美食推荐"), containing("3"), "classpath:response/web_search_test.json");
        mockChat(containing("茅台"), "classpath:response/llm_json_format_test.json");
        mockChatStream(containing("宫保鸡丁"), "classpath:response/llm_json_format_test_stream.txt");
        mockAssistantApiStream(containing("你是谁"));
        mockChat(containing("今天天气咋样"), "classpath:response/classify_json_test.json");
        mockPythonSandbox(containing("{\"int_input\":\"1\",\"str_input\":\"hello world\"}"),
            containing(Constants.TEST_WORKFLOW_EXECUTION_ID), "classpath:response/llm_code_test.json");
        mockWeatherSearch(containing("320500"), containing("encrypt-key"), "classpath:response/weather_api_test.json");

        mockAssistantApiTool();

        mockCustomModelList(containing(Constants.TEST_USER_ID));

        mockKnowledgeRetrieve(containing("0"), containing("5"), containing("盘古API调用方式"),
            "classpath:response/knowledge_retrieve_test.json");

        // 增加分支节点单元测试mock
        mockChat(containing("11冰箱"), "classpath:response/llm_json_format_branch_if_electrical.json");
        mockChat(containing("11米饭"), "classpath:response/llm_json_format_branch_elseif_food.json");
        mockChat(containing("11汽车"), "classpath:response/llm_json_format_branch_else_other.json");

        mockChat(containing("请介绍电器："), "classpath:response/llm_json_format_branch_if_electrical_result.json");
        mockChat(containing("请介绍食物："), "classpath:response/llm_json_format_branch_elseif_food_result.json");
        mockChat(containing("请介绍其他："), "classpath:response/llm_json_format_branch_else_other_result.json");

        // 意图mock
        mockChat(containing("22冰箱").and(containing("\"categories\"")), "classpath:response/classify_json_class1.json");
        mockChat(containing("22米饭").and(containing("\"categories\"")), "classpath:response/classify_json_class2.json");
        mockChat(containing("22汽车").and(containing("\"categories\"")),
            "classpath:response/classify_json_class_other.json");

        // 模型列表获取接口
        mockListInnerModels(containing("NLP"), "classpath:response/user_model_list.json");
        mockListInnerModels(containing("EmbeddingRank"), "classpath:response/user_search_model_list.json");

        mockListPresetModels(containing("EmbeddingRank"), "classpath:response/user_search_model_list.json");

        wireMockServer.stubFor(get(urlPathTemplate("/v1/{project_id}/model-service/preset-services"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile("classpath:response/preset_model_list.json"))));

        // 工作空间列表获取
        wireMockServer
            .stubFor(get(urlPathTemplate("/v1/{project_id}/workspaces")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile("classpath:response/workspace_list.json"))));

        wireMockServer.stubFor(get(urlPathTemplate("/auth/token")).willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(TestUtil.getStringFromFile("classpath:response/get_mock_token.json"))));

        // 获取知识库详情
        mockRetrieveKnowledgeRepo();

        // 上传文件到知识库
        mockUploadFile();

        // 数据源健康检查mock
        mockDBHealthCheck();

        // 公共服务mock
        mockCommonModule();
    }

    private static void initAgentServer() {
        start();

        // Agent智能生成测试
        mockCallModel(containing("agent"), "classpath:response/prologue_result.json");
        mockCallModel(containing("app"), "classpath:response/recommended_questions_result.json");
        mockCallModel(containing("prologue"), "classpath:response/prologue_result.json");
        mockCallModel(containing("question"), "classpath:response/recommended_questions_result.json");
        mockCallModel(containing("工具推荐助手"), "classpath:response/auto_add_tools_result.json");
        mockCallModel(containing("你好"), "classpath:response/auto_add_tools_result.json");
        mockCallModel(containing("起一个合适的名字"), "classpath:response/name_result.json");
        mockCallModel(containing("生成一段简短的描述"), "classpath:response/description_result.json");
    }

    private static void mockWeatherSearch(StringValuePattern cityPattern, StringValuePattern keyPattern,
        String resourceLocation) {
        wireMockServer
            .stubFor(get(urlPathMatching("/v3/weather/weatherInfo?([a-z1-9=&%]*)")).withQueryParam("city", cityPattern)
                .withQueryParam("key", keyPattern)
                .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(TestUtil.getStringFromFile(resourceLocation))));
        wireMockServer.stubFor(post(urlPathMatching("/weather?([a-z1-9=&%]*)")).withQueryParam("key", keyPattern)
            .withRequestBody(containing("320500"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }

    private static void mockWebSearch(StringValuePattern queryPattern, StringValuePattern limitPattern,
        String resourceLocation) {
        wireMockServer.stubFor(get(urlPathMatching("/v1/finance/applications/custom/websearch?([a-z1-9=&%]*)"))
            .withQueryParam("query", queryPattern)
            .withQueryParam("limit", limitPattern)
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }

    private static void mockCustomModelList(StringValuePattern userId) {
        wireMockServer.stubFor(get(urlPathMatching("/aiagent/agent/getAvailableModelByUserId.htm?([a-z1-9=&%]*)"))
            .withQueryParam("userId", userId)
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile("classpath:response/get_available_model_result.json"))));
    }

    private static void mockPythonSandbox(StringValuePattern codePattern, StringValuePattern sessionIdPattern,
        String resourceLocation) {
        wireMockServer.stubFor(post(urlEqualTo("/query")).withRequestBody(matchingJsonPath("$.code", codePattern))
            .withRequestBody(matchingJsonPath("$.session_id", sessionIdPattern))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }

    private static void mockChat(StringValuePattern pattern, String resourceLocation) {
        wireMockServer.stubFor(
            post(urlEqualTo("/chat/completions")).withRequestBody(matchingJsonPath("$.stream", equalTo("false")))
                .withRequestBody(matchingJsonPath("$.messages[0].content", pattern))
                .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }

    private static void mockCallModel(StringValuePattern pattern, String resourceLocation) {
        wireMockServer.stubFor(post(urlPathEqualTo("/v1/agent-builder/chat/completions"))  // 只匹配路径，忽略 query
            .withRequestBody(matchingJsonPath("$.messages[0].content", pattern))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }

    private static void mockChatStream(StringValuePattern pattern, String resourceLocation) {
        wireMockServer.stubFor(
            post(urlEqualTo("/chat/completions")).withRequestBody(matchingJsonPath("$.stream", equalTo("true")))
                .withRequestBody(matchingJsonPath("$.messages[0].content", pattern))
                .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }

    private static void mockAssistantApiStream(StringValuePattern pattern) {
        wireMockServer.stubFor(post(urlPathTemplate(
            "/v1/{project_id}/workspaces/default/app-dev-api/assistants/{assistant_id}/run/{session_id}/stream"))
            .withRequestBody(matchingJsonPath("$.messages[0].contents[0].content", pattern))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile("classpath:response/assistant_api_run_stream_test.txt"))));
    }

    private static void mockAssistantApiTool() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/test_project_id/workspaces/default/app-dev-api/tools"))
            .withRequestBody(matchingJsonPath("$.tool_id", containing("test")))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile("classpath:response/assistant_api_create_tool_test.json"))));

        wireMockServer
            .stubFor(put(urlPathMatching("/v1/test_project_id/workspaces/default/app-dev-api/tools/([a-z1-9=&%]*)"))
                .withRequestBody(matchingJsonPath("$.tool_desc", containing("111")))
                .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(TestUtil.getStringFromFile("classpath:response/assistant_api_modify_tool_test.json"))));

        wireMockServer
            .stubFor(delete(urlPathMatching("/v1/test_project_id/workspaces/default/app-dev-api/tools/([a-z1-9=&%]*)"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));
    }

    private static void mockKnowledgeRetrieve(StringValuePattern offsetPattern, StringValuePattern limitPattern,
        StringValuePattern bodyPattern, String resourceLocation) {
        wireMockServer.stubFor(post(
            urlPathMatching("/v1/test_project_id/workspaces/default/app-dev-api/knowledge/retrieve?([a-z1-9=&%]*)"))
            .withQueryParam("offset", offsetPattern)
            .withQueryParam("limit", limitPattern)
            .withRequestBody(matchingJsonPath("$.query", bodyPattern))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }

    // 查询模型列表，model-service内部接口
    private static void mockListInnerModels(StringValuePattern assetTypePattern, String resourceLocation) {
        wireMockServer.stubFor(
            get(urlPathTemplate("/v1/model-service/inner/services")).withQueryParam("asset_types", assetTypePattern)
                .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }

    private static void mockListPresetModels(StringValuePattern assetTypePattern, String resourceLocation) {
        wireMockServer.stubFor(get(urlPathTemplate("/v1/test_project_id/model-service/preset-services"))
            .withQueryParam("asset_types", assetTypePattern)
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }

    private static void mockRetrieveKnowledgeRepo() {
        wireMockServer.stubFor(get(urlPathMatching(
            "/v1/1ed40ceefc8d40f8b884edb6a84e7768/applications/fb9731ab-7085-474fb6c7-64473586f0f3/uni-search/knowledge-repo/[a-zA-Z0-9-_]+"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile("classpath:response/retrieve_knowledge_repo_test.json"))));

        wireMockServer.stubFor(get(urlPathMatching(
            "/v1/1ed40ceefc8d40f8b884edb6a84e7768/applications/fb9731ab-7085-474fb6c7-64473586f0f3/uni-search/knowledge-repo/[a-zA-Z0-9-_]+"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile("classpath:response/retrieve_knowledge_repo_test.json"))));
    }

    private static void mockUploadFile() {
        wireMockServer.stubFor(post(urlPathMatching(
            "/v1/1ed40ceefc8d40f8b884edb6a84e7768/applications/fb9731ab-7085-474fb6c7-64473586f0f3/uni-search/knowledge-repo/[a-zA-Z0-9-_]+/files"))
            .willReturn(
                aResponse().withStatus(200).withBody("{\"file_id\": \"e2cd7b14-e299-4214-b517-d42a0b90a753\"}")));
    }

    private static void mockDBHealthCheck() {
        wireMockServer.stubFor(
            post(urlEqualTo("/v1/db/health")).withRequestBody(matchingJsonPath("$.id", equalTo("test_workflow_id")))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));
    }

    private static void mockCommonModule() {
        wireMockServer.stubFor(post(urlPathTemplate("/v1/mgmtconsole/webapi/ros/cleandata/v1/cleannotify"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"resCode\":\"0000\",\"resMsg\":\"success\",\"result\":\"000\"}")));
    }
}
