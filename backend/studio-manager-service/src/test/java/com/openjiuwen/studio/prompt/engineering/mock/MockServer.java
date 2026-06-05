/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import lombok.extern.slf4j.Slf4j;
import utils.TestUtil;

@Slf4j
public class MockServer {
    private static WireMockServer wireMockServer;

    private static final int PORT = 10001;

    public static void init() {
        initClientServer();
    }

    private static void start() {
        if (wireMockServer != null) {
            return;
        }
        wireMockServer = new WireMockServer(options().port(PORT));
        wireMockServer.start();
    }

    private static void initClientServer() {
        start();

        // 模型列表获取接口
        wireMockServer
            .stubFor(get(urlPathTemplate("/v1/model-service/inner/services")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile("classpath:response/user_model_list.json"))));

        wireMockServer.stubFor(get(urlPathTemplate("/v1/{project_id}/model-service/preset-services"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtil.getStringFromFile("classpath:response/preset_model_list.json"))));
    }

    private static void mockCallModel(StringValuePattern pattern, String resourceLocation) {
        wireMockServer.stubFor(
            post(urlEqualTo("/v1/chat/completions")).withRequestBody(matchingJsonPath("$.messages[0].content", pattern))
                .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(TestUtil.getStringFromFile(resourceLocation))));
    }
}
