/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LakeSearchApiTest {

    @Test
    void testValues() {
        assertTrue(LakeSearchApi.values().length > 0);
    }

    @Test
    void testValueOf() {
        assertEquals(LakeSearchApi.CREATE_KNOWLEDGE_REPO, LakeSearchApi.valueOf("CREATE_KNOWLEDGE_REPO"));
        assertEquals(LakeSearchApi.SEARCH_TEXT, LakeSearchApi.valueOf("SEARCH_TEXT"));
    }

    @Test
    void testGetMethod() {
        assertEquals(MethodType.POST, LakeSearchApi.CREATE_KNOWLEDGE_REPO.getMethod());
        assertEquals(MethodType.GET, LakeSearchApi.QUERY_KNOWLEDGE_REPOS.getMethod());
        assertEquals(MethodType.PUT, LakeSearchApi.MODIFY_KNOWLEDGE_REPO.getMethod());
        assertEquals(MethodType.DELETE, LakeSearchApi.DELETE_KNOWLEDGE_REPO.getMethod());
    }

    @Test
    void testGetEndpoint() {
        assertNotNull(LakeSearchApi.CREATE_KNOWLEDGE_REPO.getEndpoint());
        assertTrue(LakeSearchApi.CREATE_KNOWLEDGE_REPO.getEndpoint().contains("/knowledge-repo"));
    }

    @Test
    void testAllValuesHaveNonNullFields() {
        for (LakeSearchApi api : LakeSearchApi.values()) {
            assertNotNull(api.getMethod());
            assertNotNull(api.getEndpoint());
        }
    }
}
