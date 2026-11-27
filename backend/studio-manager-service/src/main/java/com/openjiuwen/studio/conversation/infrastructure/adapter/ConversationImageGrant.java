/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.conversation.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.conversation.domain.model.Conversation;
import com.openjiuwen.studio.conversation.application.dto.ConversationImageCandidate;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Authenticate one Manager-approved image catalog to Runtime without exposing the internal key. */
final class ConversationImageGrant {
    private static final String PURPOSE = "conversation-image-grant:v2:";

    private ConversationImageGrant() {
    }

    static String issue(ObjectMapper mapper, String token, Conversation conversation, String appId,
                        String agentReleaseId, String executionId, List<ConversationImageCandidate> refs,
                        int omittedCurrent) {
        try {
            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            var images = refs.stream().map(candidate -> {
                var ref = candidate.ref();
                return Map.of("objectKey", ref.getObjectKey(), "fileName", ref.getFileName(),
                    "size", ref.getSize(), "mediaType", ref.getMediaType(), "checksum", ref.getChecksum(),
                    "current", candidate.current(), "ordinal", candidate.ordinal());
            }).toList();
            Map<String, Object> envelope = Map.of("conversationId", conversation.getConversationId(),
                "userId", conversation.getOwnerUserId(), "projectId", conversation.getProjectId(),
                "workspaceId", conversation.getWorkspaceId(), "appId", appId, "executionId", executionId,
                "expires", Instant.now().getEpochSecond() + 120L, "images", images,
                "agentReleaseId", agentReleaseId, "omittedCurrent", omittedCurrent);
            String data = encoder.encodeToString(mapper.writeValueAsBytes(envelope));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return data + "." + encoder.encodeToString(mac.doFinal((PURPOSE + data).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException | JsonProcessingException exception) {
            throw new IllegalStateException("Could not authorize conversation images");
        }
    }
}
