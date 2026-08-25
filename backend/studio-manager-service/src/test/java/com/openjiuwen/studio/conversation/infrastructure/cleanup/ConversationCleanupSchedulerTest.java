package com.openjiuwen.studio.conversation.infrastructure.cleanup;

import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationEntity;
import com.openjiuwen.studio.conversation.infrastructure.repository.ConversationEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConversationCleanupSchedulerTest {
    @Test
    void shouldUseAtomicClaimAndBackoff() {
        ConversationEntityRepository repository = mock(ConversationEntityRepository.class);
        ConversationCleanupOrchestrator orchestrator = mock(ConversationCleanupOrchestrator.class);
        ConversationCleanupScheduler scheduler = new ConversationCleanupScheduler(repository, orchestrator);
        ReflectionTestUtils.setField(scheduler, "maxAttempts", 8);
        ReflectionTestUtils.setField(scheduler, "baseBackoffSeconds", 30L);
        ReflectionTestUtils.setField(scheduler, "claimTimeoutSeconds", 300L);
        ConversationEntity due = candidate("c1", "PENDING", 0, new Date());
        when(repository.findByDeletedAndCleanupStatusInAndCleanupAttemptsLessThanOrderByCleanupUpdatedAtAsc(
            eq(1), anyList(), eq(8), any())).thenReturn(List.of(due));
        when(repository.claimCleanup(eq("c1"), any(), any(), eq(8))).thenReturn(1);

        scheduler.retry();

        verify(orchestrator).process("c1");
    }

    @Test
    void shouldNotProcessWhenAnotherInstanceWonClaim() {
        ConversationEntityRepository repository = mock(ConversationEntityRepository.class);
        ConversationCleanupOrchestrator orchestrator = mock(ConversationCleanupOrchestrator.class);
        ConversationCleanupScheduler scheduler = new ConversationCleanupScheduler(repository, orchestrator);
        ReflectionTestUtils.setField(scheduler, "maxAttempts", 8);
        ReflectionTestUtils.setField(scheduler, "baseBackoffSeconds", 30L);
        ReflectionTestUtils.setField(scheduler, "claimTimeoutSeconds", 300L);
        when(repository.findByDeletedAndCleanupStatusInAndCleanupAttemptsLessThanOrderByCleanupUpdatedAtAsc(
            eq(1), anyList(), eq(8), any())).thenReturn(List.of(candidate("c1", "PENDING", 0, new Date())));
        when(repository.claimCleanup(eq("c1"), any(), any(), eq(8))).thenReturn(0);

        scheduler.retry();

        verifyNoInteractions(orchestrator);
    }

    @Test
    void shouldReclaimOnlyExpiredProcessingLease() {
        ConversationEntityRepository repository = mock(ConversationEntityRepository.class);
        ConversationCleanupScheduler scheduler = new ConversationCleanupScheduler(
            repository, mock(ConversationCleanupOrchestrator.class));
        ReflectionTestUtils.setField(scheduler, "claimTimeoutSeconds", 300L);
        Date now = new Date();

        assertFalse(scheduler.due(candidate("active", "PROCESSING", 1,
            Date.from(now.toInstant().minusSeconds(60))), now));
        assertTrue(scheduler.due(candidate("expired", "PROCESSING", 1,
            Date.from(now.toInstant().minusSeconds(301))), now));
    }

    private ConversationEntity candidate(String id, String status, int attempts, Date updated) {
        ConversationEntity entity = new ConversationEntity();
        entity.setConversationId(id);
        entity.setCleanupStatus(status);
        entity.setCleanupAttempts(attempts);
        entity.setCleanupUpdatedAt(updated);
        return entity;
    }
}
