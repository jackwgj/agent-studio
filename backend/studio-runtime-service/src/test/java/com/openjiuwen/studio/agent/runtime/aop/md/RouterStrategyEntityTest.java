/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.aop.md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.entity.RouterStrategyEntity;

import org.junit.jupiter.api.Test;

public class RouterStrategyEntityTest {
    @Test
    void testRouterStrategyEntity() throws Exception {
        // 创建 RouterStrategyEntity 实例
        RouterStrategyEntity entity = new RouterStrategyEntity().setId("12345")
            .setStrategyName("Test Strategy")
            .setStrategyType("DEFAULT")
            .setStrategyKey("test_key")
            .setStrategyDescription("This is a test strategy")
            .setServiceIdList("service1,service2")
            .setStrategyTimeout(30)
            .setStrategyRetryCount(3)
            .setServiceCount(2)
            .setPrepareAttribute("{\"key\":\"value\"}")
            .setProjectId("project_123")
            .setWorkspaceId("workspace_456")
            .setDomainId("domain_789")
            .setCreatedByUserName("admin")
            .setLastUpdatedByUserName("admin")
            .setCreatedDate(System.currentTimeMillis())
            .setLastUpdatedDate(System.currentTimeMillis());

        // 验证字段是否正确设置
        assertEquals("12345", entity.getId());
        assertEquals("Test Strategy", entity.getStrategyName());
        assertEquals("DEFAULT", entity.getStrategyType());
        assertEquals("test_key", entity.getStrategyKey());
        assertEquals("This is a test strategy", entity.getStrategyDescription());
        assertEquals("service1,service2", entity.getServiceIdList());
        assertEquals(30, entity.getStrategyTimeout());
        assertEquals(3, entity.getStrategyRetryCount());
        assertEquals(2, entity.getServiceCount());
        assertEquals("{\"key\":\"value\"}", entity.getPrepareAttribute());
        assertEquals("project_123", entity.getProjectId());
        assertEquals("workspace_456", entity.getWorkspaceId());
        assertEquals("domain_789", entity.getDomainId());
        assertEquals("admin", entity.getCreatedByUserName());
        assertEquals("admin", entity.getLastUpdatedByUserName());
        assertTrue(entity.getCreatedDate() > 0);
        assertTrue(entity.getLastUpdatedDate() > 0);

        // 序列化为 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(entity);
        System.out.println("Serialized JSON: " + json);

        // 反序列化回对象
        RouterStrategyEntity deserializedEntity = objectMapper.readValue(json, RouterStrategyEntity.class);

        // 验证反序列化后的对象
        assertEquals(entity.getId(), deserializedEntity.getId());
        assertEquals(entity.getStrategyName(), deserializedEntity.getStrategyName());
        assertEquals(entity.getStrategyType(), deserializedEntity.getStrategyType());
        assertEquals(entity.getStrategyKey(), deserializedEntity.getStrategyKey());
        assertEquals(entity.getStrategyDescription(), deserializedEntity.getStrategyDescription());
        assertEquals(entity.getServiceIdList(), deserializedEntity.getServiceIdList());
        assertEquals(entity.getStrategyTimeout(), deserializedEntity.getStrategyTimeout());
        assertEquals(entity.getStrategyRetryCount(), deserializedEntity.getStrategyRetryCount());
        assertEquals(entity.getServiceCount(), deserializedEntity.getServiceCount());
        assertEquals(entity.getPrepareAttribute(), deserializedEntity.getPrepareAttribute());
        assertEquals(entity.getProjectId(), deserializedEntity.getProjectId());
        assertEquals(entity.getWorkspaceId(), deserializedEntity.getWorkspaceId());
        assertEquals(entity.getDomainId(), deserializedEntity.getDomainId());
        assertEquals(entity.getCreatedByUserName(), deserializedEntity.getCreatedByUserName());
        assertEquals(entity.getLastUpdatedByUserName(), deserializedEntity.getLastUpdatedByUserName());
        assertEquals(entity.getCreatedDate(), deserializedEntity.getCreatedDate());
        assertEquals(entity.getLastUpdatedDate(), deserializedEntity.getLastUpdatedDate());
    }
}
