/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.aop.md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.runtime.entity.md.ProviderAuthData;

import org.junit.jupiter.api.Test;

public class ProviderAuthDataTest {

    @Test
    void testProviderAuthData() throws Exception {
        // 创建 ProviderAuthData 实例
        ProviderAuthData authData = new ProviderAuthData().setId("12345")
            .setProviderId("provider_123")
            .setAuthMetadataId("metadata_456")
            .setAuthType("API_KEY")
            .setAuthInfo("{\"key\":\"value\"}")
            .setCreatedByUser("admin")
            .setCreatedDate(System.currentTimeMillis())
            .setLastUpdatedDate(System.currentTimeMillis())
            .setDomainId("domain_789")
            .setProjectId("project_101")
            .setWorkspaceId("workspace_202")
            .setIdentityId("identity_303");

        // 验证字段是否正确设置
        assertEquals("12345", authData.getId());
        assertEquals("provider_123", authData.getProviderId());
        assertEquals("metadata_456", authData.getAuthMetadataId());
        assertEquals("API_KEY", authData.getAuthType());
        assertEquals("{\"key\":\"value\"}", authData.getAuthInfo());
        assertEquals("admin", authData.getCreatedByUser());
        assertTrue(authData.getCreatedDate() > 0);
        assertTrue(authData.getLastUpdatedDate() > 0);
        assertEquals("domain_789", authData.getDomainId());
        assertEquals("project_101", authData.getProjectId());
        assertEquals("workspace_202", authData.getWorkspaceId());
        assertEquals("identity_303", authData.getIdentityId());

        // 序列化为 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(authData);
        System.out.println("Serialized JSON: " + json);

        // 反序列化回对象
        ProviderAuthData deserializedAuthData = objectMapper.readValue(json, ProviderAuthData.class);

        // 验证反序列化后的对象
        assertEquals(authData.getId(), deserializedAuthData.getId());
        assertEquals(authData.getProviderId(), deserializedAuthData.getProviderId());
        assertEquals(authData.getAuthMetadataId(), deserializedAuthData.getAuthMetadataId());
        assertEquals(authData.getAuthType(), deserializedAuthData.getAuthType());
        assertEquals(authData.getAuthInfo(), deserializedAuthData.getAuthInfo());
        assertEquals(authData.getCreatedByUser(), deserializedAuthData.getCreatedByUser());
        assertEquals(authData.getCreatedDate(), deserializedAuthData.getCreatedDate());
        assertEquals(authData.getLastUpdatedDate(), deserializedAuthData.getLastUpdatedDate());
        assertEquals(authData.getDomainId(), deserializedAuthData.getDomainId());
        assertEquals(authData.getProjectId(), deserializedAuthData.getProjectId());
        assertEquals(authData.getWorkspaceId(), deserializedAuthData.getWorkspaceId());
        assertEquals(authData.getIdentityId(), deserializedAuthData.getIdentityId());
    }
}
