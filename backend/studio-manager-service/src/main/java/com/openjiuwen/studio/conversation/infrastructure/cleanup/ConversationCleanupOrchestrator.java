/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.cleanup;

import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationEntity;
import com.openjiuwen.studio.conversation.infrastructure.repository.ConversationEntityRepository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/** Coordinates idempotent cleanup across object storage and Runtime workspace. */
@Service
public class ConversationCleanupOrchestrator {
    static final int MAX_ERROR_LENGTH = 1024;

    private final ConversationEntityRepository conversations;
    private final MgObsService obs;
    private final ConversationRuntimeCleanupAdapter runtime;

    public ConversationCleanupOrchestrator(ConversationEntityRepository conversations, MgObsService obs,
                                           ConversationRuntimeCleanupAdapter runtime) {
        this.conversations = conversations;
        this.obs = obs;
        this.runtime = runtime;
    }

    public void process(String conversationId) {
        ConversationEntity conversation = conversations.findById(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("conversation not found: " + conversationId));
        try {
            if (!obs.deleteByPrefix(ConversationArtifactPrefix.from(conversation))) {
                throw new IllegalStateException("object storage cleanup returned false");
            }
            if (!runtime.deleteWorkspace(conversation)) {
                throw new IllegalStateException("runtime workspace cleanup returned false");
            }
            conversation.setCleanupStatus("DONE");
            conversation.setCleanupError(null);
        } catch (Exception exception) {
            conversation.setCleanupStatus("FAILED");
            conversation.setCleanupError(StringUtils.abbreviate(exception.getMessage(), MAX_ERROR_LENGTH));
        }
        conversation.setCleanupUpdatedAt(new Date());
        conversations.save(conversation);
    }
}
