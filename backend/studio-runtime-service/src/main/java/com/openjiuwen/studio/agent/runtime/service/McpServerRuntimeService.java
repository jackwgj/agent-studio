/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.dto.mcp.McpValidationResp;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.dto.run.AuthInfo;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.runtime.dto.McpCallToolReq;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolResp;
import com.openjiuwen.studio.agent.common.dto.mcp.McpValidationReq;
import com.openjiuwen.studio.agent.runtime.mcp.McpServerClient;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteServer;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteValidation;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;

import io.modelcontextprotocol.spec.McpSchema;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mcp 服务运行态接口定义
 *
 */
@Service
public class McpServerRuntimeService implements IMcpServerRuntimeService {
    private final McpServerClient mcpClient;

    public McpServerRuntimeService(McpServerClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    /**
     * Mcp 服务工具调用
     *
     * @param projectId 租户 id
     * @param body 请求体
     * @return 工具运行结果
     */
    @Override
    public McpCallToolResp callTool(String projectId, McpCallToolReq body) {
        decryptAuthInfo(body.getAuth());
        McpSchema.CallToolResult result = mcpClient.callTool(body.getUrl(), getQueryString(body.getAuth()),
            getHeaders(body.getAuth()), body.getName(), body.getParams());
        McpCallToolResp toolResp = JsonUtils.json2ObjQuietly(JsonUtils.toJson(result), McpCallToolResp.class);
        if (toolResp != null && toolResp.isIsError() == null) {
            toolResp.setIsError(false);
        }
        return toolResp;
    }

    /**
     * 查询 mcp 服务工具列表 (内部接口)
     * auth 参数需要加密
     *
     * @param projectId 租户 id
     * @param body 请求体
     * @return 工具列表
     */
    @Override
    public Object listMcpServerTools(String projectId, McpValidationReq body) {
        decryptAuthInfo(body.getAuth());
        McpRemoteServer remoteServer =
            mcpClient.getRemoteServerInfo(body.getUrl(), getQueryString(body.getAuth()), getHeaders(body.getAuth()));
        return remoteServer.getTools();
    }

    /**
     * Mcp 服务连通性测试
     *
     * @param projectId 租户 id
     * @param body 请求体
     * @return 连通性测试结果
     */
    @Override
    public McpValidationResp testServer(String projectId, McpValidationReq body) {
        McpRemoteValidation validation =
            mcpClient.checkServerValid(body.getUrl(), getQueryString(body.getAuth()), getHeaders(body.getAuth()));
        return new McpValidationResp().setSuccess(validation.isSuccess()).setErrorMsg(validation.getErrorMsg());
    }

    private String getQueryString(AuthInfo authInfo) {
        if (authInfo == null || authInfo.getAuthKeys() == null) {
            return "";
        }
        if (!authInfo.getDomain().equals(AuthInfo.DomainEnum.QUERY)) {
            return "";
        }
        List<String> queries = new ArrayList<>();
        for (AuthKeyInfo keyInfo : authInfo.getAuthKeys()) {
            queries.add(keyInfo.getTargetName() + "=" + keyInfo.getAuthKey());
        }
        return String.join("&", queries);
    }

    private Map<String, String> getHeaders(AuthInfo authInfo) {
        Map<String, String> headers = new HashMap<>();
        if (authInfo == null || authInfo.getAuthKeys() == null) {
            return headers;
        }
        if (!authInfo.getDomain().equals(AuthInfo.DomainEnum.HEADERS)) {
            return headers;
        }
        for (AuthKeyInfo keyInfo : authInfo.getAuthKeys()) {
            headers.put(keyInfo.getTargetName(), keyInfo.getAuthKey());
        }
        return headers;
    }

    private void decryptAuthInfo(AuthInfo authInfo) {
        if (authInfo == null || authInfo.getAuthKeys() == null) {
            return;
        }
        authInfo.getAuthKeys().forEach(authKeyInfo -> {
            if (StringUtils.isNotBlank(authKeyInfo.getAuthKey())) {
                authKeyInfo.setAuthKey(CryptoUtils.decrypt(authKeyInfo.getAuthKey()));
            }
        });
    }
}
