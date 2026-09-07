package com.openjiuwen.studio.conversation.infrastructure.cleanup;

import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConversationArtifactPrefixTest {
    @Test
    void shouldDeriveOneConversationFormalArtifactPrefixFromPersistedIdentity() {
        ConversationEntity entity = new ConversationEntity();
        entity.setProjectId("project/../unsafe");
        entity.setWorkspaceId("workspace");
        entity.setOwnerUserId("user");
        entity.setConversationId("conversation");

        String prefix = ConversationArtifactPrefix.from(entity);

        assertTrue(prefix.startsWith("conversation-artifacts/"));
        assertTrue(prefix.endsWith("/"));
        assertEquals(6, prefix.split("/", -1).length);
        assertFalse(prefix.contains("unsafe"));
        assertFalse(prefix.contains("skills"));
        assertFalse(prefix.contains("conversation-inputs"));
    }
}
