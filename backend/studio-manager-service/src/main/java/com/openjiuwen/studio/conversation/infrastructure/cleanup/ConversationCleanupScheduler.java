/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.cleanup;

import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationEntity;
import com.openjiuwen.studio.conversation.infrastructure.repository.ConversationEntityRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/** Bounded, database-claimed cleanup retries for deleted conversations. */
@Component
public class ConversationCleanupScheduler {
    private final ConversationEntityRepository conversations;
    private final ConversationCleanupOrchestrator orchestrator;

    @Value("${conversation.cleanup.max-attempts:8}")
    private int maxAttempts;

    @Value("${conversation.cleanup.base-backoff-seconds:30}")
    private long baseBackoffSeconds;

    @Value("${conversation.cleanup.claim-timeout-seconds:300}")
    private long claimTimeoutSeconds;

    public ConversationCleanupScheduler(ConversationEntityRepository conversations,
                                        ConversationCleanupOrchestrator orchestrator) {
        this.conversations = conversations;
        this.orchestrator = orchestrator;
    }

    @Scheduled(fixedDelayString = "${conversation.cleanup.retry-interval-ms:30000}")
    public void retry() {
        Date now = new Date();
        List<ConversationEntity> candidates = conversations
            .findByDeletedAndCleanupStatusInAndCleanupAttemptsLessThanOrderByCleanupUpdatedAtAsc(
                1, List.of("PENDING", "FAILED", "PROCESSING"), maxAttempts, PageRequest.of(0, 20));
        candidates.stream().filter(item -> due(item, now)).forEach(item -> {
            Date leaseExpiredAt = Date.from(now.toInstant().minusSeconds(claimTimeoutSeconds));
            if (conversations.claimCleanup(item.getConversationId(), now, leaseExpiredAt, maxAttempts) == 1) {
                orchestrator.process(item.getConversationId());
            }
        });
    }

    boolean due(ConversationEntity item, Date now) {
        if ("PROCESSING".equals(item.getCleanupStatus())) {
            Instant updated = item.getCleanupUpdatedAt() == null
                ? Instant.EPOCH : item.getCleanupUpdatedAt().toInstant();
            return !updated.plusSeconds(claimTimeoutSeconds).isAfter(now.toInstant());
        }
        if ("PENDING".equals(item.getCleanupStatus()) && item.getCleanupAttempts() == 0) {
            return true;
        }
        int exponent = Math.min(Math.max(item.getCleanupAttempts() - 1, 0), 10);
        long seconds = Math.min(baseBackoffSeconds * (1L << exponent), 3600L);
        Instant updated = item.getCleanupUpdatedAt() == null ? Instant.EPOCH : item.getCleanupUpdatedAt().toInstant();
        return !updated.plus(Duration.ofSeconds(seconds)).isAfter(now.toInstant());
    }
}
