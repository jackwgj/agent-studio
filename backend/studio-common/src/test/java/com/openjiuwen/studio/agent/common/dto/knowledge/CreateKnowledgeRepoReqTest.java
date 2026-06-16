/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

class CreateKnowledgeRepoReqTest {

    @Test
    void testBuilder() {
        CreateKnowledgeRepoReq req = CreateKnowledgeRepoReq.builder()
                .name("Test Repo")
                .detail("detail")
                .embeddingModel("model-a")
                .reRankModel("model-b")
                .nlpModel("model-c")
                .repoType("shared")
                .languageId("lang-001")
                .cacheEnabled(true)
                .answerReferenceEnabled(true)
                .answerImageReferenceEnabled(false)
                .searchPlanCategoryIds(List.of("cat1", "cat2"))
                .build();
        assertEquals("Test Repo", req.getName());
        assertEquals("detail", req.getDetail());
        assertEquals("model-a", req.getEmbeddingModel());
        assertEquals("model-b", req.getReRankModel());
        assertEquals("model-c", req.getNlpModel());
        assertEquals("shared", req.getRepoType());
        assertEquals("lang-001", req.getLanguageId());
        assertTrue(req.isCacheEnabled());
        assertTrue(req.isAnswerReferenceEnabled());
        assertFalse(req.isAnswerImageReferenceEnabled());
        assertEquals(2, req.getSearchPlanCategoryIds().size());
    }

    @Test
    void testNoArgsConstructor() {
        CreateKnowledgeRepoReq req = new CreateKnowledgeRepoReq();
        assertNotNull(req);
        assertNull(req.getName());
    }

    @Test
    void testEqualsAndHashCode() {
        CreateKnowledgeRepoReq r1 = CreateKnowledgeRepoReq.builder().name("Repo").detail("d").build();
        CreateKnowledgeRepoReq r2 = CreateKnowledgeRepoReq.builder().name("Repo").detail("d").build();
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testEquals_Different() {
        CreateKnowledgeRepoReq r1 = CreateKnowledgeRepoReq.builder().name("Repo1").build();
        CreateKnowledgeRepoReq r2 = CreateKnowledgeRepoReq.builder().name("Repo2").build();
        assertNotEquals(r1, r2);
    }

    @Test
    void testToString() {
        CreateKnowledgeRepoReq req = CreateKnowledgeRepoReq.builder().name("Repo").build();
        String str = req.toString();
        assertNotNull(str);
        assertTrue(str.contains("Repo"));
    }

    @Test
    void testSessionConfig_Builder() {
        CreateKnowledgeRepoReq.SessionConfig config = CreateKnowledgeRepoReq.SessionConfig.builder()
                .similarityThreshold(0.8f)
                .answerSelectPolicy("top_k")
                .build();
        assertEquals(0.8f, config.getSimilarityThreshold());
        assertEquals("top_k", config.getAnswerSelectPolicy());
    }

    @Test
    void testSessionConfig_EqualsAndHashCode() {
        CreateKnowledgeRepoReq.SessionConfig c1 = CreateKnowledgeRepoReq.SessionConfig.builder()
                .similarityThreshold(0.8f).build();
        CreateKnowledgeRepoReq.SessionConfig c2 = CreateKnowledgeRepoReq.SessionConfig.builder()
                .similarityThreshold(0.8f).build();
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testEviction_Builder() {
        CreateKnowledgeRepoReq.SessionConfig.Eviction eviction = CreateKnowledgeRepoReq.SessionConfig.Eviction.builder()
                .policy("lru")
                .ttl(3600L)
                .hitCountThreshold(5L)
                .build();
        assertEquals("lru", eviction.getPolicy());
        assertEquals(3600L, eviction.getTtl());
        assertEquals(5L, eviction.getHitCountThreshold());
    }

    @Test
    void testEviction_EqualsAndHashCode() {
        CreateKnowledgeRepoReq.SessionConfig.Eviction e1 = CreateKnowledgeRepoReq.SessionConfig.Eviction.builder()
                .policy("lru").ttl(3600L).build();
        CreateKnowledgeRepoReq.SessionConfig.Eviction e2 = CreateKnowledgeRepoReq.SessionConfig.Eviction.builder()
                .policy("lru").ttl(3600L).build();
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testEviction_NoArgsConstructor() {
        CreateKnowledgeRepoReq.SessionConfig.Eviction eviction = new CreateKnowledgeRepoReq.SessionConfig.Eviction();
        assertNotNull(eviction);
        assertNull(eviction.getPolicy());
    }

    @Test
    void testSessionConfig_NoArgsConstructor() {
        CreateKnowledgeRepoReq.SessionConfig config = new CreateKnowledgeRepoReq.SessionConfig();
        assertNotNull(config);
        assertNull(config.getSimilarityThreshold());
    }

    @Test
    void testRepoIndexConfig() {
        RepoIndexConfig config = RepoIndexConfig.builder().shareRepoLimit(10).build();
        CreateKnowledgeRepoReq req = CreateKnowledgeRepoReq.builder()
                .repoIndexConfig(config)
                .build();
        assertEquals(10, req.getRepoIndexConfig().getShareRepoLimit());
    }

    @Test
    void testFileExtract() {
        FileExtract extract = FileExtract.builder().build();
        CreateKnowledgeRepoReq req = CreateKnowledgeRepoReq.builder()
                .fileExtract(extract)
                .build();
        assertNotNull(req.getFileExtract());
    }
}
