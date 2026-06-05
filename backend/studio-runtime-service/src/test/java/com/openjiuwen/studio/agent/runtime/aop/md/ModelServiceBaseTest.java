/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.aop.md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.runtime.entity.md.ModelServiceBase;

import org.junit.jupiter.api.Test;

public class ModelServiceBaseTest {

    @Test
    void testModelServiceBase() throws Exception {
        // 创建 ModelServiceBase 实例
        ModelServiceBase modelService = new ModelServiceBase().setId("12345")
            .setProviderId("provider_123")
            .setServiceName("test_service")
            .setServiceKey("service_key_456")
            .setModelName("test_model")
            .setModelVersion("1.0.0")
            .setModelType("AI")
            .setModelTags("tag1,tag2")
            .setModelDescription("This is a test model")
            .setModelDeployType("ON_PREMISES")
            .setDocumentUrl("http://example.com")
            .setModelSize(100.5f)
            .setContextLength(1024)
            .setModelPriority(1)
            .setDomainId("domain_789")
            .setProjectId("project_101")
            .setWorkspaceId("workspace_202")
            .setCreatedByUser("admin")
            .setLastUpdatedByUser("admin")
            .setCreatedDate(System.currentTimeMillis())
            .setLastUpdatedDate(System.currentTimeMillis())
            .setApiUrl("http://api.example.com")
            .setIsReasoning(true)
            .setIsSupportFunction(false)
            .setAuthMetadataId("auth_303")
            .setIdentityId("identity_404")
            .setPublishStatus("PUBLISHED")
            .setInterfaceProtocol("HTTP")
            .setIsSupportStream(true);

        // 验证字段是否正确设置
        assertEquals("12345", modelService.getId());
        assertEquals("provider_123", modelService.getProviderId());
        assertEquals("test_service", modelService.getServiceName());
        assertEquals("service_key_456", modelService.getServiceKey());
        assertEquals("test_model", modelService.getModelName());
        assertEquals("1.0.0", modelService.getModelVersion());
        assertEquals("AI", modelService.getModelType());
        assertEquals("tag1,tag2", modelService.getModelTags());
        assertEquals("This is a test model", modelService.getModelDescription());
        assertEquals("ON_PREMISES", modelService.getModelDeployType());
        assertEquals("http://example.com", modelService.getDocumentUrl());
        assertEquals(100.5f, modelService.getModelSize());
        assertEquals(1024, modelService.getContextLength());
        assertEquals(1, modelService.getModelPriority());
        assertEquals("domain_789", modelService.getDomainId());
        assertEquals("project_101", modelService.getProjectId());
        assertEquals("workspace_202", modelService.getWorkspaceId());
        assertEquals("admin", modelService.getCreatedByUser());
        assertEquals("admin", modelService.getLastUpdatedByUser());
        assertTrue(modelService.getCreatedDate() > 0);
        assertTrue(modelService.getLastUpdatedDate() > 0);
        assertEquals("http://api.example.com", modelService.getApiUrl());
        assertTrue(modelService.getIsReasoning());
        assertFalse(modelService.getIsSupportFunction());
        assertEquals("auth_303", modelService.getAuthMetadataId());
        assertEquals("identity_404", modelService.getIdentityId());
        assertEquals("PUBLISHED", modelService.getPublishStatus());
        assertEquals("HTTP", modelService.getInterfaceProtocol());
        assertTrue(modelService.getIsSupportStream());

        // 序列化为 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(modelService);
        System.out.println("Serialized JSON: " + json);

        // 反序列化回对象
        ModelServiceBase deserializedModelService = objectMapper.readValue(json, ModelServiceBase.class);

        // 验证反序列化后的对象
        assertEquals(modelService.getId(), deserializedModelService.getId());
        assertEquals(modelService.getProviderId(), deserializedModelService.getProviderId());
        assertEquals(modelService.getServiceName(), deserializedModelService.getServiceName());
        assertEquals(modelService.getServiceKey(), deserializedModelService.getServiceKey());
        assertEquals(modelService.getModelName(), deserializedModelService.getModelName());
        assertEquals(modelService.getModelVersion(), deserializedModelService.getModelVersion());
        assertEquals(modelService.getModelType(), deserializedModelService.getModelType());
        assertEquals(modelService.getModelTags(), deserializedModelService.getModelTags());
        assertEquals(modelService.getModelDescription(), deserializedModelService.getModelDescription());
        assertEquals(modelService.getModelDeployType(), deserializedModelService.getModelDeployType());
        assertEquals(modelService.getDocumentUrl(), deserializedModelService.getDocumentUrl());
        assertEquals(modelService.getModelSize(), deserializedModelService.getModelSize());
        assertEquals(modelService.getContextLength(), deserializedModelService.getContextLength());
        assertEquals(modelService.getModelPriority(), deserializedModelService.getModelPriority());
        assertEquals(modelService.getDomainId(), deserializedModelService.getDomainId());
        assertEquals(modelService.getProjectId(), deserializedModelService.getProjectId());
        assertEquals(modelService.getWorkspaceId(), deserializedModelService.getWorkspaceId());
        assertEquals(modelService.getCreatedByUser(), deserializedModelService.getCreatedByUser());
        assertEquals(modelService.getLastUpdatedByUser(), deserializedModelService.getLastUpdatedByUser());
        assertEquals(modelService.getCreatedDate(), deserializedModelService.getCreatedDate());
        assertEquals(modelService.getLastUpdatedDate(), deserializedModelService.getLastUpdatedDate());
        assertEquals(modelService.getApiUrl(), deserializedModelService.getApiUrl());
        assertEquals(modelService.getIsReasoning(), deserializedModelService.getIsReasoning());
        assertEquals(modelService.getIsSupportFunction(), deserializedModelService.getIsSupportFunction());
        assertEquals(modelService.getAuthMetadataId(), deserializedModelService.getAuthMetadataId());
        assertEquals(modelService.getIdentityId(), deserializedModelService.getIdentityId());
        assertEquals(modelService.getPublishStatus(), deserializedModelService.getPublishStatus());
        assertEquals(modelService.getInterfaceProtocol(), deserializedModelService.getInterfaceProtocol());
        assertEquals(modelService.getIsSupportStream(), deserializedModelService.getIsSupportStream());
    }
}
