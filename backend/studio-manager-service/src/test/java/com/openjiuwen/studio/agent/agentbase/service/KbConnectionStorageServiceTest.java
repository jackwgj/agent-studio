/* Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved. */
package com.openjiuwen.studio.agent.agentbase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.enums.KnowledgeBaseConnectionStatus;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.model.obs.KbConnectionObsData;
import com.openjiuwen.studio.agent.agentbase.model.obs.KbKnowledgeBaseObsData;
import com.openjiuwen.studio.agent.foundation.base.utils.JacksonUtils;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

/**
 * 校验知识库连接/知识库 OBS 文件的 JSON 字段契约（驼峰 key + knowledgeSource），
 * 这是 Python 运行时读取端依赖的协议，序列化结果必须与历史 Map 实现保持一致。
 */
class KbConnectionStorageServiceTest {

    private KbConnectionStorageService service;

    @BeforeEach
    void setUp() {
        service = new KbConnectionStorageService(Mockito.mock(MgObsService.class));
        ReflectionTestUtils.setField(service, "knowledgeSource", "CUSTOM");
    }

    private KnowledgeBaseConnectionEntity sampleConnection() {
        KnowledgeBaseConnectionEntity conn = new KnowledgeBaseConnectionEntity();
        conn.setId("conn-1");
        conn.setConnectorId("LakeSearchInside");
        conn.setName("my-conn");
        conn.setStatus(KnowledgeBaseConnectionStatus.OPEN);
        conn.setKrb5File("krb5");
        conn.setKeytabFile("keytab");
        conn.setParams(List.of(
            new KnowledgeBaseConnectionParam("endpoint", "http://host"),
            new KnowledgeBaseConnectionParam("password", "cipher")
        ));
        return conn;
    }

    @Test
    void buildConnectionData_serializesCamelCaseKeysWithKnowledgeSource() {
        KbConnectionObsData data = ReflectionTestUtils.invokeMethod(
            service, "buildConnectionData", sampleConnection(), "inside", "LakeSearch");
        assertNotNull(data);

        JsonNode json = JacksonUtils.readValue(JacksonUtils.toJson(data), JsonNode.class);
        // 驼峰 key（命名策略不可改写）+ 新增 knowledgeSource 标记
        assertEquals("conn-1", json.get("connectionId").asText());
        assertEquals("LakeSearchInside", json.get("connectorId").asText());
        assertEquals("inside", json.get("connectorType").asText());
        assertEquals("LakeSearch", json.get("connectorName").asText());
        assertEquals("CUSTOM", json.get("knowledgeSource").asText());
        assertEquals("OPEN", json.get("status").asText());
        // params 子对象 code/value
        assertTrue(json.get("params").isArray());
        assertEquals("endpoint", json.get("params").get(0).get("code").asText());
        assertEquals("http://host", json.get("params").get(0).get("value").asText());
    }

    @Test
    void buildKnowledgeBaseData_serializesCamelCaseKeys() {
        KnowledgeBaseEntity kb = new KnowledgeBaseEntity();
        kb.setId("kb-1");
        kb.setName("kb-name");
        kb.setType("INTERNAL");
        kb.setExternalId("ext-1");
        kb.setStatus("OPEN");
        kb.setKnowledgeBaseConnectionId("conn-1");

        KbKnowledgeBaseObsData data = ReflectionTestUtils.invokeMethod(
            service, "buildKnowledgeBaseData", kb);
        assertNotNull(data);

        JsonNode json = JacksonUtils.readValue(JacksonUtils.toJson(data), JsonNode.class);
        assertEquals("kb-1", json.get("knowledgeBaseId").asText());
        assertEquals("kb-name", json.get("knowledgeBaseName").asText());
        assertEquals("INTERNAL", json.get("type").asText());
        assertEquals("ext-1", json.get("externalId").asText());
        assertEquals("conn-1", json.get("connectionId").asText());
    }

    @Test
    void connectionData_omitsNullFields() {
        KnowledgeBaseConnectionEntity conn = new KnowledgeBaseConnectionEntity();
        conn.setId("conn-2");
        conn.setConnectorId("Ragflow");
        // name/status/krb5File 等为 null —— NON_NULL 策略应省略

        KbConnectionObsData data = ReflectionTestUtils.invokeMethod(
            service, "buildConnectionData", conn, "outside", "Ragflow");
        JsonNode json = JacksonUtils.readValue(JacksonUtils.toJson(data), JsonNode.class);

        assertEquals("conn-2", json.get("connectionId").asText());
        assertTrue(json.has("knowledgeSource"));
        // null 字段被省略
        org.junit.jupiter.api.Assertions.assertFalse(json.has("name"));
        org.junit.jupiter.api.Assertions.assertFalse(json.has("status"));
    }
}
