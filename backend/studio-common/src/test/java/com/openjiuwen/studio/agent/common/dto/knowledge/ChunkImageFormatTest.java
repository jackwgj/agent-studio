/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChunkImageFormatTest {

    @Test
    void testBuilder() {
        ChunkImageFormat format = ChunkImageFormat.builder()
                .knowledgeBaseId("kb-1")
                .imageAccessMaps(Map.of("img1", "key1"))
                .imageIds(List.of("img1"))
                .chunkResult("result text")
                .build();

        assertEquals("kb-1", format.getKnowledgeBaseId());
        assertEquals(1, format.getImageAccessMaps().size());
        assertEquals("key1", format.getImageAccessMaps().get("img1"));
        assertEquals(1, format.getImageIds().size());
        assertEquals("img1", format.getImageIds().get(0));
        assertEquals("result text", format.getChunkResult());
    }

    @Test
    void testSettersAndGetters() {
        ChunkImageFormat format = ChunkImageFormat.builder().build();
        format.setKnowledgeBaseId("kb-2");
        format.setImageAccessMaps(Map.of("a", "b"));
        format.setImageIds(List.of("a"));
        format.setChunkResult("chunk");

        assertEquals("kb-2", format.getKnowledgeBaseId());
        assertEquals("b", format.getImageAccessMaps().get("a"));
        assertEquals("a", format.getImageIds().get(0));
        assertEquals("chunk", format.getChunkResult());
    }

    @Test
    void testBuilderMinimal() {
        ChunkImageFormat format = ChunkImageFormat.builder().build();
        assertNotNull(format);
        assertNull(format.getKnowledgeBaseId());
        assertNull(format.getImageAccessMaps());
        assertNull(format.getImageIds());
        assertNull(format.getChunkResult());
    }
}
