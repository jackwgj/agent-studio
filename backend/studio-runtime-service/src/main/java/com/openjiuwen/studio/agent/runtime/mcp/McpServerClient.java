/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mcp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteServer;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteTool;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteTools;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteValidation;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

/**
 * MCP 客户端
 *
 */
@Component
@Slf4j
public class McpServerClient {
    private static final int DEFAULT_TIMEOUT = 30;

    private static final String OUTPUT_SCHEME =
        "{\"type\":\"object\",\"properties\":{\"isError\":{\"type\":\"boolean\"},\"content\":{\"type\":\"array\","
            + "\"items\":{\"type\":\"object\",\"properties\":{\"type\":{\"type\":\"string\"},\"text\":{\"type\":"
            + "\"string\"}},\"required\":[\"type\",\"text\"],\"additionalProperties\":false}}},\"required\":"
            + "[\"content\"],\"additionalProperties\":false}";

    private SSLContext sslContext = null;

    public McpServerClient() {
        try {
            // MCP 连接忽略 SSL 证书
            sslContext = new SSLContextBuilder().setSecureRandom(SecureRandom.getInstanceStrong())
                .loadTrustMaterial(null, (arg0, arg1) -> true)
                .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.error("[McpClientUtils]: Create ssl failed should not happen", e);
        }
    }

    /**
     * 获取 Mcp 服务和工具信息
     *
     * @param url mcp 服务 url
     * @param query query 信息
     * @param headers header 信息
     * @return mcp 服务信息（服务名，工具列表）
     */
    public McpRemoteServer getRemoteServerInfo(String url, String query, Map<String, String> headers) {
        McpSyncClient client = getClient(url, query, headers);
        // 初始化并获取服务端信息
        client.initialize();

        McpRemoteServer remoteServer = new McpRemoteServer();
        remoteServer.setName(client.getServerInfo().name());
        remoteServer.setVersion(client.getServerInfo().version());

        // 获取 MCP 服务工具
        List<McpRemoteTool> tools = new ArrayList<>();
        McpSchema.ListToolsResult toolsResult = client.listTools();
        while (toolsResult != null) {
            toolsResult.tools().forEach(tool -> {
                String inputString = JsonUtils.toJson(tool.inputSchema());
                McpRemoteTool serverTool = McpRemoteTool.builder()
                    .name(tool.name())
                    .desc(tool.description())
                    .input(inputString)
                    .output(OUTPUT_SCHEME)
                    .paramsCount(paramsCount(inputString))
                    .build();
                tools.add(serverTool);
            });
            if (StringUtils.isNotBlank(toolsResult.nextCursor())) {
                toolsResult = client.listTools(toolsResult.nextCursor());
            } else {
                break;
            }
        }
        client.close();
        McpRemoteTools mcpRemoteTools = new McpRemoteTools();
        mcpRemoteTools.setCount(tools.size());
        mcpRemoteTools.setTools(tools);
        remoteServer.setTools(mcpRemoteTools);

        return remoteServer;
    }

    /**
     * 连通性测试
     *
     * @param url mcp 服务地址
     * @param query query 信息
     * @param headers header 信息
     * @return 连通性测试结果
     */
    @SuppressWarnings("checkstyle: all")
    public McpRemoteValidation checkServerValid(String url, String query, Map<String, String> headers) {
        boolean isSuccess = true;
        String errorMsg = "";
        McpSyncClient client = getClient(url, query, headers);
        try {
            client.initialize();
            client.close();
        } catch (Exception e) {
            log.error("[MCP]: test server error ", e);
            isSuccess = false;
            if (e.getCause() instanceof TimeoutException) {
                errorMsg = "connect timeout, check the url or network";
            } else {
                errorMsg = e.getMessage();
            }
        }

        return new McpRemoteValidation(isSuccess, errorMsg);
    }

    /**
     * mcp 服务工具调用
     *
     * @param url mcp 服务地址
     * @param query query 参数
     * @param headers header 参数
     * @param method 要调用的工具名称
     * @param params 工具参数
     * @return 工具运行结果
     */
    public McpSchema.CallToolResult callTool(String url, String query, Map<String, String> headers, String method,
        Map<String, Object> params) {
        McpSyncClient client = getClient(url, query, headers);
        // 初始化并获取服务端信息
        client.initialize();
        McpSchema.ListToolsResult toolsResult = client.listTools();
        McpSchema.Tool mcpTool = null;
        while (toolsResult != null) {
            mcpTool = toolsResult.tools().stream().filter(tool -> tool.name().equals(method)).findAny().orElse(null);
            if (mcpTool == null && StringUtils.isNotBlank(toolsResult.nextCursor())) {
                toolsResult = client.listTools(toolsResult.nextCursor());
            } else {
                break;
            }
        }
        McpSchema.CallToolResult result;
        if (mcpTool == null) {
            result = new McpSchema.CallToolResult("mcp tool with name: " + "<" + method + ">" + " is not find", true);
        } else {
            try {
                result = client.callTool(new McpSchema.CallToolRequest(method, params));
            } catch (McpError e) {
                result = new McpSchema.CallToolResult(e.getMessage(), true);
            }
        }
        client.close();
        return result;
    }

    private McpSyncClient getClient(String url, String query, Map<String, String> headers) {
        if (StringUtils.isBlank(url)) {
            log.error("[mcp]: url is empty");
            throw new AgentStudioException(StudioError.MCP_REQUEST_ERROR);
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            log.error("mcp server url parse failed.");
            throw new AgentStudioException(StudioError.MCP_REQUEST_ERROR);
        }
        String baseUrl = uri.getScheme() + "://" + uri.getHost();
        if (uri.getPort() > 0) {
            baseUrl = baseUrl + ":" + uri.getPort();
        }
        String sseEndpoint = uri.getPath();
        if (StringUtils.isNotBlank(uri.getQuery())) {
            sseEndpoint = sseEndpoint + "?" + uri.getQuery();
        }
        if (StringUtils.isNotBlank(query)) {
            sseEndpoint = sseEndpoint + (StringUtils.isNotBlank(uri.getQuery()) ? "&" : "?") + query;
        }

        // === 重构开始 ===
        // 1. 提前构建并配置 HttpClient.Builder (替代 .customizeClient)
        // 这里处理 SSL 和 超时
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT));
        if (this.sslContext != null) { // 假设 sslContext 是类成员变量
            clientBuilder.sslContext(this.sslContext);
        }

        // 2. 提前构建并配置 HttpRequest.Builder (替代 .customizeRequest)
        // 这里处理 Headers
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestBuilder::header);
        }
        // 3. 构建 Transport
        // 移除 .objectMapper()
        // 使用 .clientBuilder() 和 .requestBuilder() 注入配置
        var transport = HttpClientSseClientTransport.builder(baseUrl)
            .sseEndpoint(sseEndpoint)
            .clientBuilder(clientBuilder)   // 注入配置好的 HTTP 客户端构建器
            .requestBuilder(requestBuilder) // 注入配置好的请求构建器
            .build();
        // 4. 构建 Client
        return McpClient.sync(transport)
            .requestTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
            .capabilities(McpSchema.ClientCapabilities.builder().build())
            .build();
        // === 重构结束 ===
    }

    private int paramsCount(String paramString) {
        if (StringUtils.isBlank(paramString)) {
            return 0;
        }

        ParamsJsonSchema jsonSchema = JsonUtils.json2ObjQuietly(paramString, ParamsJsonSchema.class);
        if (jsonSchema == null || jsonSchema.properties == null) {
            return 0;
        }
        return jsonSchema.properties.size();
    }

    /**
     * 工具入参 schema
     *
     * @param type
     * @param properties
     * @param required
     * @param isAdditionalProperties
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ParamsJsonSchema(String type, Map<String, Object> properties, List<String> required,
        boolean isAdditionalProperties) {
    }
}
