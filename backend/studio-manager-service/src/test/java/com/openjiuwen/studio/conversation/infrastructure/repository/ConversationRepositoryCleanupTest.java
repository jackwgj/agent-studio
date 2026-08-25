/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationRepositoryCleanupTest {
    @Test
    void shouldAtomicallyMarkDeletedAndPendingCleanup() {
        ConversationEntityRepository entities = mock(ConversationEntityRepository.class);
        when(entities.markDeletedAndPendingCleanup(eq("c1"), any())).thenReturn(1);
        ConversationRepositoryImpl repository = new ConversationRepositoryImpl(entities,
            mock(ConversationRunEntityRepository.class), mock(ConversationSubRunEntityRepository.class));

        repository.softDeleteAndScheduleCleanup("c1");

        verify(entities).markDeletedAndPendingCleanup(eq("c1"), any());
    }

    @Test
    void shouldPropagateDatabaseFailureForTransactionRollback() {
        ConversationEntityRepository entities = mock(ConversationEntityRepository.class);
        when(entities.markDeletedAndPendingCleanup(eq("c1"), any()))
            .thenThrow(new IllegalStateException("database unavailable"));
        ConversationRepositoryImpl repository = new ConversationRepositoryImpl(entities,
            mock(ConversationRunEntityRepository.class), mock(ConversationSubRunEntityRepository.class));

        assertThrows(IllegalStateException.class, () -> repository.softDeleteAndScheduleCleanup("c1"));
    }
}
