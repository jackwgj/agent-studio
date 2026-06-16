/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.bo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AgentMetadataTest {

    @Test
    void testBuilder() {
        AgentMetadata metadata = AgentMetadata.builder()
                .agentId("agent-1")
                .domainId("domain-1")
                .projectId("project-1")
                .workspaceId("workspace-1")
                .versionId("version-1")
                .updatedAt(1000L)
                .isContentReviewEnable(true)
                .safetyBarrier(true)
                .appType("workflow")
                .build();

        assertEquals("agent-1", metadata.getAgentId());
        assertEquals("domain-1", metadata.getDomainId());
        assertEquals("project-1", metadata.getProjectId());
        assertEquals("workspace-1", metadata.getWorkspaceId());
        assertEquals("version-1", metadata.getVersionId());
        assertEquals(1000L, metadata.getUpdatedAt());
        assertEquals(true, metadata.isContentReviewEnable());
        assertEquals(true, metadata.isSafetyBarrier());
        assertEquals("workflow", metadata.getAppType());
    }

    @Test
    void testSettersAndGetters() {
        AgentMetadata metadata = AgentMetadata.builder().build();
        metadata.setAgentId("a1");
        metadata.setDomainId("d1");
        metadata.setProjectId("p1");
        metadata.setWorkspaceId("w1");
        metadata.setVersionId("v1");
        metadata.setUpdatedAt(2000L);
        metadata.setContentReviewEnable(false);
        metadata.setSafetyBarrier(false);
        metadata.setAppType("agent");

        assertEquals("a1", metadata.getAgentId());
        assertEquals("d1", metadata.getDomainId());
        assertEquals("p1", metadata.getProjectId());
        assertEquals("w1", metadata.getWorkspaceId());
        assertEquals("v1", metadata.getVersionId());
        assertEquals(2000L, metadata.getUpdatedAt());
        assertFalse(metadata.isContentReviewEnable());
        assertFalse(metadata.isSafetyBarrier());
        assertEquals("agent", metadata.getAppType());
    }

    @Test
    void testBuilderMinimal() {
        AgentMetadata metadata = AgentMetadata.builder().build();
        assertNotNull(metadata);
    }

    @Test
    void testDefaultBooleanValues() {
        AgentMetadata metadata = AgentMetadata.builder().build();
        assertFalse(metadata.isContentReviewEnable());
        assertFalse(metadata.isSafetyBarrier());
        assertEquals(0L, metadata.getUpdatedAt());
    }
}
