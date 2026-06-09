/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openjiuwen.studio.agent.agentbase.constant.Constant;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.KooSearchConnection;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@MockitoSettings(strictness = Strictness.LENIENT)
class KooSearchConnectionProviderTest {

    @Mock
    private KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper;

    private KooSearchConnectionProvider
        kooSearchConnectionProvider;

    Cache<String, KnowledgeBaseConnectionEntity> knowledgeBaseConnectionEntityCacheByConnectionId
        = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(5, TimeUnit.MINUTES).build();

    Cache<String, KnowledgeBaseConnectionEntity> knowledgeBaseConnectionEntityCacheByRepoId = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();

    Cache<String, KnowledgeBaseConnectionEntity> knowledgeBaseConnectionEntityCacheBySegmentId = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();

    @BeforeEach
    void init() {
        kooSearchConnectionProvider = new KooSearchConnectionProvider(knowledgeBaseConnectionMapper);

        ReflectionTestUtils.setField(kooSearchConnectionProvider, "knowledgeBaseConnectionEntityCacheByConnectionId",
            knowledgeBaseConnectionEntityCacheByConnectionId);

        ReflectionTestUtils.setField(kooSearchConnectionProvider, "knowledgeBaseConnectionEntityCacheByRepoId",
            knowledgeBaseConnectionEntityCacheByRepoId);

        ReflectionTestUtils.setField(kooSearchConnectionProvider, "knowledgeBaseConnectionEntityCacheBySegmentId",
            knowledgeBaseConnectionEntityCacheBySegmentId);

        ReflectionTestUtils.setField(kooSearchConnectionProvider, "knowledgeSource", "KooSearch");
    }

    @Test
    void getKnowledgeSourceConnectionFromCache() {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();
        knowledgeBaseConnectionEntity.setId("testConnectionId");
        List<KnowledgeBaseConnectionParam> params = new ArrayList<>();
        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam1 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam1.setCode("auth_mode");
        knowledgeBaseConnectionParam1.setValue("none");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam2 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam2.setCode("endpoint");
        knowledgeBaseConnectionParam2.setValue("http://1.1.1.1:10");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam3 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam3.setCode("application_id");
        knowledgeBaseConnectionParam3.setValue("application_id_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam4 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam4.setCode("project_id");
        knowledgeBaseConnectionParam4.setValue("project_id_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam5 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam5.setCode("ocr_enable");
        knowledgeBaseConnectionParam5.setValue("false");

        params.add(knowledgeBaseConnectionParam1);
        params.add(knowledgeBaseConnectionParam2);
        params.add(knowledgeBaseConnectionParam3);
        params.add(knowledgeBaseConnectionParam4);
        params.add(knowledgeBaseConnectionParam5);

        knowledgeBaseConnectionEntity.setParams(params);
        knowledgeBaseConnectionEntity.setConnectorId("KooSearchInside");
        knowledgeBaseConnectionEntityCacheByConnectionId.put("testConnectionId", knowledgeBaseConnectionEntity);
        KooSearchConnection kooSearchConnection = kooSearchConnectionProvider.getKnowledgeSourceConnection(
            "testConnectionId");
        Assertions.assertEquals(kooSearchConnection.getProjectId(), "project_id_test");
        Assertions.assertEquals(kooSearchConnection.getEndpoint(), "http://1.1.1.1:10");
    }

    @Test
    void getKnowledgeSourceConnectionFromDB() {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();
        knowledgeBaseConnectionEntity.setId("testConnectionId");
        List<KnowledgeBaseConnectionParam> params = new ArrayList<>();
        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam1 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam1.setCode("auth_mode");
        knowledgeBaseConnectionParam1.setValue("token");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam2 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam2.setCode("endpoint");
        knowledgeBaseConnectionParam2.setValue("http://1.1.1.1:10");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam3 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam3.setCode("application_id");
        knowledgeBaseConnectionParam3.setValue("application_id_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam4 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam4.setCode("project_id");
        knowledgeBaseConnectionParam4.setValue("project_id_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam5 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam5.setCode("ocr_enable");
        knowledgeBaseConnectionParam5.setValue("false");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam6 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam6.setCode("project_name");
        knowledgeBaseConnectionParam6.setValue("project_name_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam7 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam7.setCode("domain_name");
        knowledgeBaseConnectionParam7.setValue("domain_name_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam8 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam8.setCode("user_name");
        knowledgeBaseConnectionParam8.setValue("user_name_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam9 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam9.setCode("user_password");
        knowledgeBaseConnectionParam9.setValue("user_password_test");

        params.add(knowledgeBaseConnectionParam1);
        params.add(knowledgeBaseConnectionParam2);
        params.add(knowledgeBaseConnectionParam3);
        params.add(knowledgeBaseConnectionParam4);
        params.add(knowledgeBaseConnectionParam5);
        params.add(knowledgeBaseConnectionParam6);
        params.add(knowledgeBaseConnectionParam7);
        params.add(knowledgeBaseConnectionParam8);
        params.add(knowledgeBaseConnectionParam9);

        knowledgeBaseConnectionEntity.setParams(params);
        knowledgeBaseConnectionEntity.setConnectorId("KooSearchInside");
        when(knowledgeBaseConnectionMapper.findById(eq("testConnectionId"))).thenReturn(knowledgeBaseConnectionEntity);
        KooSearchConnection kooSearchConnection = kooSearchConnectionProvider.getKnowledgeSourceConnection(
            "testConnectionId");
        Assertions.assertEquals(kooSearchConnection.getProjectId(), "project_id_test");
        Assertions.assertEquals(kooSearchConnection.getEndpoint(), "http://1.1.1.1:10");
        Assertions.assertEquals(kooSearchConnection.getUserName(), "user_name_test");
    }

    @Test
    void getKnowledgeSourceConnectionFromDBAppcode() {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();
        knowledgeBaseConnectionEntity.setId("testConnectionId");
        List<KnowledgeBaseConnectionParam> params = new ArrayList<>();
        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam1 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam1.setCode("auth_mode");
        knowledgeBaseConnectionParam1.setValue("app_code");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam2 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam2.setCode("endpoint");
        knowledgeBaseConnectionParam2.setValue("http://1.1.1.1:10");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam3 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam3.setCode("application_id");
        knowledgeBaseConnectionParam3.setValue("application_id_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam4 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam4.setCode("project_id");
        knowledgeBaseConnectionParam4.setValue("project_id_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam5 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam5.setCode("ocr_enable");
        knowledgeBaseConnectionParam5.setValue("false");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam6 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam6.setCode("app_code");
        knowledgeBaseConnectionParam6.setValue("app_code_test");

        params.add(knowledgeBaseConnectionParam1);
        params.add(knowledgeBaseConnectionParam2);
        params.add(knowledgeBaseConnectionParam3);
        params.add(knowledgeBaseConnectionParam4);
        params.add(knowledgeBaseConnectionParam5);
        params.add(knowledgeBaseConnectionParam6);

        knowledgeBaseConnectionEntity.setParams(params);
        knowledgeBaseConnectionEntity.setConnectorId("KooSearchInside");
        when(knowledgeBaseConnectionMapper.findById(eq("testConnectionId"))).thenReturn(knowledgeBaseConnectionEntity);
        KooSearchConnection kooSearchConnection = kooSearchConnectionProvider.getKnowledgeSourceConnection(
            "testConnectionId");
        Assertions.assertEquals(kooSearchConnection.getProjectId(), "project_id_test");
        Assertions.assertEquals(kooSearchConnection.getEndpoint(), "http://1.1.1.1:10");
        Assertions.assertEquals(kooSearchConnection.getAppCode(), "app_code_test");
    }

    @Test
    void getConnectionIdByRepoIdTest() {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();
        knowledgeBaseConnectionEntity.setId("testConnectionId");
        List<KnowledgeBaseConnectionParam> params = new ArrayList<>();
        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam1 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam1.setCode("auth_mode");
        knowledgeBaseConnectionParam1.setValue("none");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam2 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam2.setCode("endpoint");
        knowledgeBaseConnectionParam2.setValue("http://1.1.1.1:10");

        params.add(knowledgeBaseConnectionParam1);
        params.add(knowledgeBaseConnectionParam2);

        knowledgeBaseConnectionEntity.setParams(params);
        knowledgeBaseConnectionEntity.setConnectorId("KooSearchInside");
        knowledgeBaseConnectionEntity.setId("testConnectionId");

        when(knowledgeBaseConnectionMapper.findByExternalRepoId(eq("testRepoId"))).thenReturn(
            knowledgeBaseConnectionEntity);
        String connectionIdFromDb = kooSearchConnectionProvider.getConnectionIdByRepoId("testRepoId");
        Assertions.assertEquals(connectionIdFromDb, "testConnectionId");

        String connectionIdFromCache = kooSearchConnectionProvider.getConnectionIdByRepoId("testRepoId");
        Assertions.assertEquals(connectionIdFromCache, "testConnectionId");

    }

    @Test
    void getConnectionIdBySegmentIdTest() {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();
        knowledgeBaseConnectionEntity.setId("testConnectionId");
        List<KnowledgeBaseConnectionParam> params = new ArrayList<>();
        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam1 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam1.setCode("auth_mode");
        knowledgeBaseConnectionParam1.setValue("none");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam2 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam2.setCode("endpoint");
        knowledgeBaseConnectionParam2.setValue("http://1.1.1.1:10");

        params.add(knowledgeBaseConnectionParam1);
        params.add(knowledgeBaseConnectionParam2);

        knowledgeBaseConnectionEntity.setParams(params);
        knowledgeBaseConnectionEntity.setConnectorId("KooSearchInside");
        knowledgeBaseConnectionEntity.setId("testConnectionId");

        when(knowledgeBaseConnectionMapper.findConnectionBySegmentId(eq("testSegId"))).thenReturn(
            knowledgeBaseConnectionEntity);
        String connectionIdFromDb = kooSearchConnectionProvider.getConnectionIdBySegmentId("testSegId");
        Assertions.assertEquals(connectionIdFromDb, "testConnectionId");

        String connectionIdFromCache = kooSearchConnectionProvider.getConnectionIdBySegmentId("testSegId");
        Assertions.assertEquals(connectionIdFromCache, "testConnectionId");

    }

    @Test
    void test_getKnowledgeSourceConnection_throw_exception() {
        Assertions.assertThrows(AgentBaseException.class, () -> {
            kooSearchConnectionProvider.getKnowledgeSourceConnection(Constant.TEST_KNOWLEDGE_BASE_CONNECTION_ID);
        });
    }
}