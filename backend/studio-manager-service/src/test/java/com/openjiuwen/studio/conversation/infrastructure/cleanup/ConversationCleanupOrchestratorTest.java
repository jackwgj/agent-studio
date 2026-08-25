package com.openjiuwen.studio.conversation.infrastructure.cleanup;

import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationEntity;
import com.openjiuwen.studio.conversation.infrastructure.repository.ConversationEntityRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

class ConversationCleanupOrchestratorTest {
    @Test
    void shouldOnlyMarkDoneWhenBothSidesSucceed() throws Exception {
        ConversationEntityRepository repository = mock(ConversationEntityRepository.class);
        MgObsService obs = mock(MgObsService.class);
        ConversationRuntimeCleanupAdapter runtime = mock(ConversationRuntimeCleanupAdapter.class);
        ConversationEntity entity = entity();
        when(repository.findById("c1")).thenReturn(Optional.of(entity));
        when(obs.deleteByPrefix(startsWith("conversation-artifacts/"))).thenReturn(true);
        when(runtime.deleteWorkspace(entity)).thenReturn(true);

        new ConversationCleanupOrchestrator(repository, obs, runtime).process("c1");

        assertEquals("DONE", entity.getCleanupStatus());
        verify(repository).save(entity);
    }

    @Test
    void shouldRemainRetryableWhenEitherSideFails() throws IOException {
        ConversationEntityRepository repository = mock(ConversationEntityRepository.class);
        MgObsService obs = mock(MgObsService.class);
        ConversationRuntimeCleanupAdapter runtime = mock(ConversationRuntimeCleanupAdapter.class);
        ConversationEntity entity = entity();
        when(repository.findById("c1")).thenReturn(Optional.of(entity));
        when(obs.deleteByPrefix(anyString())).thenReturn(true);
        when(runtime.deleteWorkspace(entity)).thenThrow(new IOException("runtime unavailable"));

        new ConversationCleanupOrchestrator(repository, obs, runtime).process("c1");

        assertEquals("FAILED", entity.getCleanupStatus());
        assertEquals("runtime unavailable", entity.getCleanupError());
    }

    @Test
    void shouldTruncatePersistedFailureAndSucceedAfterRecovery() throws Exception {
        ConversationEntityRepository repository = mock(ConversationEntityRepository.class);
        MgObsService obs = mock(MgObsService.class);
        ConversationRuntimeCleanupAdapter runtime = mock(ConversationRuntimeCleanupAdapter.class);
        ConversationEntity entity = entity();
        when(repository.findById("c1")).thenReturn(Optional.of(entity));
        when(obs.deleteByPrefix(anyString())).thenReturn(true);
        when(runtime.deleteWorkspace(entity))
            .thenThrow(new IOException("x".repeat(2048)))
            .thenReturn(true);
        ConversationCleanupOrchestrator orchestrator = new ConversationCleanupOrchestrator(repository, obs, runtime);

        orchestrator.process("c1");
        assertEquals("FAILED", entity.getCleanupStatus());
        assertEquals(1024, entity.getCleanupError().length());
        orchestrator.process("c1");
        assertEquals("DONE", entity.getCleanupStatus());
        assertEquals(null, entity.getCleanupError());
    }

    private ConversationEntity entity() {
        ConversationEntity entity = new ConversationEntity();
        entity.setConversationId("c1");
        entity.setProjectId("p1");
        entity.setWorkspaceId("w1");
        entity.setOwnerUserId("u1");
        entity.setDeleted(1);
        entity.setCleanupStatus("PROCESSING");
        return entity;
    }
}
