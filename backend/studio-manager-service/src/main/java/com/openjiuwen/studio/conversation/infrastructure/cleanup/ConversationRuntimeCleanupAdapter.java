/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.cleanup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationEntity;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/** Manager-to-Runtime internal conversation workspace cleanup client. */
@Component
public class ConversationRuntimeCleanupAdapter {
    private final OkHttpClientUtils clients;
    private final ObjectMapper objectMapper;

    @Value("${agent_runtime_endpoint:http://127.0.0.1:31014}")
    private String runtimeEndpoint;

    @Value("${conversation.cleanup.internal-token:}")
    private String internalToken;

    public ConversationRuntimeCleanupAdapter(OkHttpClientUtils clients, ObjectMapper objectMapper) {
        this.clients = clients;
        this.objectMapper = objectMapper;
    }

    public boolean deleteWorkspace(ConversationEntity conversation) throws IOException {
        if (internalToken == null || internalToken.isBlank()) {
            throw new IllegalStateException("conversation.cleanup.internal-token is required");
        }
        Map<String, String> body = Map.of(
            "projectId", conversation.getProjectId(),
            "workspaceId", conversation.getWorkspaceId(),
            "userId", conversation.getOwnerUserId(),
            "conversationId", conversation.getConversationId());
        Request request = new Request.Builder()
            .url(runtimeEndpoint + "/internal/v1/conversation/workspace/cleanup")
            .header("X-Conversation-Cleanup-Token", internalToken)
            .post(RequestBody.create(objectMapper.writeValueAsBytes(body), MediaType.parse("application/json")))
            .build();
        try (Response response = clients.getHttpClient().newCall(request).execute()) {
            return response.isSuccessful();
        }
    }
}
