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
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.LakeSearchConnection;
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
class LakeSearchConnectionProviderTest {

    @Mock
    private KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper;

    private com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider.LakeSearchConnectionProvider
        lakeSearchConnectionProvider;

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
        lakeSearchConnectionProvider = new LakeSearchConnectionProvider(knowledgeBaseConnectionMapper);

        ReflectionTestUtils.setField(lakeSearchConnectionProvider, "knowledgeBaseConnectionEntityCacheByConnectionId",
            knowledgeBaseConnectionEntityCacheByConnectionId);

        ReflectionTestUtils.setField(lakeSearchConnectionProvider, "knowledgeBaseConnectionEntityCacheByRepoId",
            knowledgeBaseConnectionEntityCacheByRepoId);

        ReflectionTestUtils.setField(lakeSearchConnectionProvider, "knowledgeBaseConnectionEntityCacheBySegmentId",
            knowledgeBaseConnectionEntityCacheBySegmentId);

        ReflectionTestUtils.setField(lakeSearchConnectionProvider, "knowledgeSource", "LakeSearch");
    }

    @Test
    void getKnowledgeSourceConnectionBasicMode() {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();
        knowledgeBaseConnectionEntity.setId("testConnectionId");
        List<KnowledgeBaseConnectionParam> params = new ArrayList<>();
        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam1 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam1.setCode("auth_mode");
        knowledgeBaseConnectionParam1.setValue("basic");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam2 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam2.setCode("endpoint");
        knowledgeBaseConnectionParam2.setValue("http://3.3.3.3:10");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam3 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam3.setCode("authorization");
        knowledgeBaseConnectionParam3.setValue("authorization_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam4 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam4.setCode("ocr_enable");
        knowledgeBaseConnectionParam4.setValue("false");

        params.add(knowledgeBaseConnectionParam1);
        params.add(knowledgeBaseConnectionParam2);
        params.add(knowledgeBaseConnectionParam3);
        params.add(knowledgeBaseConnectionParam4);

        knowledgeBaseConnectionEntity.setParams(params);
        knowledgeBaseConnectionEntity.setConnectorId("LakeSearchInside");

        when(knowledgeBaseConnectionMapper.findById(eq("testConnectionId"))).thenReturn(knowledgeBaseConnectionEntity);
        LakeSearchConnection lakeSearchConnection = lakeSearchConnectionProvider.getKnowledgeSourceConnection(
            "testConnectionId");
        Assertions.assertEquals(lakeSearchConnection.getEndpoint(), "http://3.3.3.3:10");
        LakeSearchConnection lakeSearchConnectionCache = lakeSearchConnectionProvider.getKnowledgeSourceConnection(
            "testConnectionId");
        Assertions.assertEquals(lakeSearchConnectionCache.getEndpoint(), "http://3.3.3.3:10");
        Assertions.assertEquals(lakeSearchConnectionCache.getLsBasicAuth().getLakeSearchAuthorization(),
            "authorization_test");
    }

    @Test
    void getKnowledgeSourceConnectionKerberosMode() {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();
        knowledgeBaseConnectionEntity.setId("testConnectionId");
        List<KnowledgeBaseConnectionParam> params = new ArrayList<>();
        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam1 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam1.setCode("auth_mode");
        knowledgeBaseConnectionParam1.setValue("kerberos");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam2 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam2.setCode("endpoint");
        knowledgeBaseConnectionParam2.setValue("http://3.3.3.3:10");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam3 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam3.setCode("authorization");
        knowledgeBaseConnectionParam3.setValue("authorization_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam4 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam4.setCode("ocr_enable");
        knowledgeBaseConnectionParam4.setValue("false");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam5 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam5.setCode("cluster_ips");
        knowledgeBaseConnectionParam5.setValue("1.2.3.4,4.5.6.7,7.8.9.9");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam6 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam6.setCode("port");
        knowledgeBaseConnectionParam6.setValue("8080");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam7 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam7.setCode("protocol");
        knowledgeBaseConnectionParam7.setValue("https");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam8 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam8.setCode("user_keytab_file");
        knowledgeBaseConnectionParam8.setValue("user_keytab_file_test");

        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam9 = new KnowledgeBaseConnectionParam();
        knowledgeBaseConnectionParam9.setCode("krb5_file");
        knowledgeBaseConnectionParam9.setValue("krb5_file_test");

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
        knowledgeBaseConnectionEntity.setConnectorId("LakeSearchInside");

        when(knowledgeBaseConnectionMapper.findById(eq("testConnectionId"))).thenReturn(knowledgeBaseConnectionEntity);
        LakeSearchConnection lakeSearchConnection = lakeSearchConnectionProvider.getKnowledgeSourceConnection(
            "testConnectionId");
        Assertions.assertEquals(lakeSearchConnection.getKerberosAuth().getClusterIps(), "1.2.3.4,4.5.6.7,7.8.9.9");
        LakeSearchConnection lakeSearchConnectionCache = lakeSearchConnectionProvider.getKnowledgeSourceConnection(
            "testConnectionId");
        Assertions.assertEquals(lakeSearchConnectionCache.getEndpoint(), "http://3.3.3.3:10");
        Assertions.assertEquals(lakeSearchConnectionCache.getKerberosAuth().getKrb5File(), "krb5_file_test");
    }

    @Test
    void test_getKnowledgeSourceConnection_throw_exception() {
        Assertions.assertThrows(AgentBaseException.class, () -> {
            lakeSearchConnectionProvider.getKnowledgeSourceConnection(Constant.TEST_KNOWLEDGE_BASE_CONNECTION_ID);
        });
    }

}