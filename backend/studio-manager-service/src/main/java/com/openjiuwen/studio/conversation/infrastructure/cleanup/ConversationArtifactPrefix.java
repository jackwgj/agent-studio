/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.cleanup;

import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Server-derived object-storage prefix for one conversation's formal artifacts. */
public final class ConversationArtifactPrefix {
    private ConversationArtifactPrefix() {
    }

    public static String from(ConversationEntity conversation) {
        return "conversation-artifacts/" + key(conversation.getProjectId()) + "/"
            + key(conversation.getWorkspaceId()) + "/" + key(conversation.getOwnerUserId()) + "/"
            + key(conversation.getConversationId()) + "/";
    }

    private static String key(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
