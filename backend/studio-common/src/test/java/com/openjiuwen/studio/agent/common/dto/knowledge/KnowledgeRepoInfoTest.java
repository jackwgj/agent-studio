/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.List;

class KnowledgeRepoInfoTest {

    @Test
    void testBuilder() {
        KnowledgeRepoInfo info = KnowledgeRepoInfo.builder()
                .id("repo-001")
                .name("Test Repo")
                .detail("detail")
                .status("active")
                .createUser("user1")
                .createTime("2024-01-01")
                .repoType("shared")
                .shareRepoUsed(5)
                .shareRepoQuota(10)
                .updateTime("2024-06-01")
                .embeddingModel("model-a")
                .rerankModel("model-b")
                .nlpModel("model-c")
                .build();
        assertEquals("repo-001", info.getId());
        assertEquals("Test Repo", info.getName());
        assertEquals("detail", info.getDetail());
        assertEquals("active", info.getStatus());
        assertEquals("user1", info.getCreateUser());
        assertEquals("2024-01-01", info.getCreateTime());
        assertEquals("shared", info.getRepoType());
        assertEquals(5, info.getShareRepoUsed());
        assertEquals(10, info.getShareRepoQuota());
        assertEquals("2024-06-01", info.getUpdateTime());
        assertEquals("model-a", info.getEmbeddingModel());
        assertEquals("model-b", info.getRerankModel());
        assertEquals("model-c", info.getNlpModel());
    }

    @Test
    void testNoArgsConstructor() {
        KnowledgeRepoInfo info = new KnowledgeRepoInfo();
        assertNotNull(info);
        assertNull(info.getId());
        assertNull(info.getName());
    }

    @Test
    void testSettersAndGetters() {
        KnowledgeRepoInfo info = new KnowledgeRepoInfo();
        info.setId("repo-001");
        info.setName("Test Repo");
        info.setDetail("detail");
        info.setStatus("active");
        info.setCreateUser("user1");
        info.setCreateTime("2024-01-01");
        info.setRepoType("shared");
        info.setShareRepoUsed(5);
        info.setShareRepoQuota(10);
        info.setUpdateTime("2024-06-01");
        info.setEmbeddingModel("model-a");
        info.setRerankModel("model-b");
        info.setNlpModel("model-c");
        assertEquals("repo-001", info.getId());
        assertEquals("Test Repo", info.getName());
        assertEquals("model-a", info.getEmbeddingModel());
    }

    @Test
    void testEqualsAndHashCode() {
        KnowledgeRepoInfo i1 = KnowledgeRepoInfo.builder().id("repo-001").name("Repo").build();
        KnowledgeRepoInfo i2 = KnowledgeRepoInfo.builder().id("repo-001").name("Repo").build();
        assertEquals(i1, i2);
        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    void testEquals_Different() {
        KnowledgeRepoInfo i1 = KnowledgeRepoInfo.builder().id("repo-001").build();
        KnowledgeRepoInfo i2 = KnowledgeRepoInfo.builder().id("repo-002").build();
        assertNotEquals(i1, i2);
    }

    @Test
    void testToString() {
        KnowledgeRepoInfo info = KnowledgeRepoInfo.builder().id("repo-001").name("Repo").build();
        String str = info.toString();
        assertNotNull(str);
        assertEquals(true, str.contains("repo-001"));
    }

    @Test
    void testFieldSchema_Builder() {
        KnowledgeRepoInfo.KnowledgeRepoFieldSchema schema = KnowledgeRepoInfo.KnowledgeRepoFieldSchema.builder()
                .name("field1")
                .fieldType("string")
                .nameZh("字段1")
                .build();
        assertEquals("field1", schema.getName());
        assertEquals("string", schema.getFieldType());
        assertEquals("字段1", schema.getNameZh());
    }

    @Test
    void testFieldSchema_EqualsAndHashCode() {
        KnowledgeRepoInfo.KnowledgeRepoFieldSchema s1 = KnowledgeRepoInfo.KnowledgeRepoFieldSchema.builder()
                .name("f1").fieldType("string").build();
        KnowledgeRepoInfo.KnowledgeRepoFieldSchema s2 = KnowledgeRepoInfo.KnowledgeRepoFieldSchema.builder()
                .name("f1").fieldType("string").build();
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void testFieldSchema_NoArgsConstructor() {
        KnowledgeRepoInfo.KnowledgeRepoFieldSchema schema = new KnowledgeRepoInfo.KnowledgeRepoFieldSchema();
        assertNotNull(schema);
        assertNull(schema.getName());
    }

    @Test
    void testFieldsList() {
        KnowledgeRepoInfo.KnowledgeRepoFieldSchema schema = KnowledgeRepoInfo.KnowledgeRepoFieldSchema.builder()
                .name("f1").build();
        KnowledgeRepoInfo info = KnowledgeRepoInfo.builder()
                .fields(List.of(schema))
                .build();
        assertEquals(1, info.getFields().size());
        assertEquals("f1", info.getFields().get(0).getName());
    }

    @Test
    void testFileExtract() {
        KnowledgeRepoInfo info = KnowledgeRepoInfo.builder()
                .fileExtract(FileExtract.builder().build())
                .build();
        assertNotNull(info.getFileExtract());
    }
}
