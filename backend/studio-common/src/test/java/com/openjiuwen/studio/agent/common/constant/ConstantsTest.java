/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConstantsTest {

    @Test
    void testTopLevelConstants() {
        assertEquals("latest", Constants.LATEST_PUBLISH_VERSION);
        assertEquals("treasure", Constants.TREASURE_PUBLISH_VERSION);
        assertEquals("metadata", Constants.OBS_METADATA_DIR);
        assertEquals("_", Constants.UNDERLINE_STR);
        assertEquals("id", Constants.ID);
        assertEquals("version_id", Constants.VERSION_ID);
        assertEquals("name", Constants.NAME);
        assertEquals("zh", Constants.ZH_LANGUAGE_ID);
        assertEquals("zh-cn", Constants.ZH_CN);
        assertEquals("tool_id", Constants.TOOL_ID);
        assertEquals("true", Constants.TRUE);
        assertEquals("ToolUse", Constants.TOOL_USE);
        assertEquals("AUTHENTICATED", Constants.AUTHENTICATED);
        assertEquals("UNAUTHENTICATED", Constants.UNAUTHENTICATED);
        assertEquals("NONE", Constants.AUTHENTICATED_NONE);
        assertEquals("data:image/", Constants.IMAGE_BASE64);
        assertEquals("memory_config", Constants.MEMORY_CONFIG);
    }

    @Test
    void testMapTypeReference() {
        assertNotNull(Constants.MAP_TYPE_REFERENCE);
    }

    @Test
    void testHeaderConstants() {
        assertEquals("X-Auth-Token", Constants.Header.X_AUTH_TOKEN);
        assertEquals("X-Language", Constants.Header.X_LANGUAGE);
        assertEquals("Authorization", Constants.Header.AUTHORIZATION);
        assertEquals("Bearer ", Constants.Header.AUTHORIZATION_PREFIX);
        assertEquals("X-Subject-Token", Constants.Header.X_SUBJECT_TOKEN);
    }

    @Test
    void testCustomModelConstants() {
        assertEquals("cust-userid", Constants.CustomModel.CUSTOM_USER_ID);
        assertEquals("cust-token", Constants.CustomModel.CUSTOM_TOKEN);
    }

    @Test
    void testAppTypeConstants() {
        assertEquals("workflow", Constants.AppType.WORKFLOW);
        assertEquals("agent", Constants.AppType.AGENT);
        assertEquals("controller", Constants.AppType.CONTROLLER);
        assertEquals("agent_new", Constants.AppType.AGENT_NEW);
    }

    @Test
    void testWorkflowConstants() {
        assertEquals("configs", Constants.Workflow.CONFIGS);
        assertEquals("nodes", Constants.Workflow.NODES);
        assertEquals("content_review", Constants.Workflow.CONTENT_REVIEW);
        assertEquals("safety_barrier", Constants.Workflow.SAFETY_BARRIER);
        assertEquals("enabled", Constants.Workflow.ENABLED);
        assertEquals("user_profile", Constants.Workflow.USER_PROFILE);
        assertEquals("enable_memory", Constants.Workflow.ENABLE_MEMORY);
        assertEquals("memory", Constants.Workflow.MEMORY);
        assertEquals("memory_config", Constants.Workflow.MEMORY_CONFIG);
        assertEquals("environment", Constants.Workflow.ENVIRONMENT);
        assertEquals(1000, Constants.Workflow.CR_KEYWORDS_MAX_LENGTH);
        assertEquals(10, Constants.Workflow.CR_GROUP_MAX_SIZE);
        assertEquals(1000, Constants.Workflow.INPUT_TEXT_MAX_LENGTH);
        assertEquals(1000, Constants.Workflow.OUTPUT_TEXT_MAX_LENGTH);
        assertEquals(1000, Constants.Workflow.REPLACE_WORD_MAX_LENGTH);
    }

    @Test
    void testWorkflowEventConstants() {
        assertEquals("event", Constants.Workflow.Event.EVENT);
        assertEquals("data", Constants.Workflow.Event.DATA);
        assertEquals("createdTime", Constants.Workflow.Event.CREATED_TIME);
    }

    @Test
    void testKnowledgeBaseConstants() {
        assertEquals("thirdPartyKnowledgeBase", Constants.KnowledgeBase.THIRD_PARTY_KNOWLEDGE_BASE);
        assertEquals("knowledgeBase", Constants.KnowledgeBase.KNOWLEDGE_BASE);
        assertEquals("top_k", Constants.KnowledgeBase.TOP_K);
        assertEquals(5, Constants.KnowledgeBase.DEFAULT_TOP_K);
        assertEquals("recall_threshold", Constants.KnowledgeBase.RECALL_THRESHOLD);
        assertEquals(0.5f, Constants.KnowledgeBase.DEFAULT_RECALL_THRESHOLD);
        assertEquals("faq_threshold", Constants.KnowledgeBase.FAQ_THRESHOLD);
        assertEquals(0.9f, Constants.KnowledgeBase.DEFAULT_FAQ_THRESHOLD);
        assertEquals("search_mode", Constants.KnowledgeBase.SEARCH_MODE);
        assertEquals("retrieve_image", Constants.KnowledgeBase.RETRIEVE_IMAGE);
    }

    @Test
    void testEnvTypeConstants() {
        assertEquals("simple", Constants.EnvType.SIMPLE);
        assertEquals("hc", Constants.EnvType.HC);
        assertEquals("hcs", Constants.EnvType.HCS);
    }
}
