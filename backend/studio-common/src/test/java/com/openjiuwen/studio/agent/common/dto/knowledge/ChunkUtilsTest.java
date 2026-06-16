/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkUtilsTest {

    @Test
    void testExtractImageId_ValidChunk() {
        String chunk = "text {img-abc123} more text {img-def456}";
        List<String> ids = ChunkUtils.extractImageId(chunk);
        assertEquals(2, ids.size());
        assertEquals("img-abc123", ids.get(0));
        assertEquals("img-def456", ids.get(1));
    }

    @Test
    void testExtractImageId_NoImages() {
        List<String> ids = ChunkUtils.extractImageId("plain text");
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    void testExtractImageId_BlankChunk() {
        List<String> ids = ChunkUtils.extractImageId("");
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    void testExtractImageId_NullChunk() {
        List<String> ids = ChunkUtils.extractImageId(null);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    void testRemoveImageId() {
        String chunk = "text {img-abc123} more text";
        String result = ChunkUtils.removeImageId(chunk);
        assertEquals("text  more text", result);
    }

    @Test
    void testRemoveImageId_NoImages() {
        String result = ChunkUtils.removeImageId("plain text");
        assertEquals("plain text", result);
    }

    @Test
    void testReplaceImageAccessKeyAndFormat_WithRetrieve() {
        String chunk = "text {img-abc123} end";
        ChunkImageFormat result = ChunkUtils.replaceImageAccessKeyAndFormat(chunk, true);
        assertNotNull(result);
        assertNotNull(result.getChunkResult());
        assertFalse(result.getChunkResult().contains("{img-abc123}"));
        assertTrue(result.getChunkResult().contains("![img](https://agent_arts_knowledge_img_url/"));
        assertNotNull(result.getImageAccessMaps());
        assertFalse(result.getImageAccessMaps().isEmpty());
    }

    @Test
    void testReplaceImageAccessKeyAndFormat_WithoutRetrieve() {
        String chunk = "text {img-abc123} end";
        ChunkImageFormat result = ChunkUtils.replaceImageAccessKeyAndFormat(chunk, false);
        assertNotNull(result);
        assertEquals("text  end", result.getChunkResult());
        assertTrue(result.getImageAccessMaps().isEmpty());
    }

    @Test
    void testReplaceImageAccessKeyAndFormat_BlankChunk() {
        ChunkImageFormat result = ChunkUtils.replaceImageAccessKeyAndFormat("", true);
        assertNotNull(result);
        assertEquals("", result.getChunkResult());
    }

    @Test
    void testReplaceImageAccessKeyAndFormat_NullChunk() {
        ChunkImageFormat result = ChunkUtils.replaceImageAccessKeyAndFormat(null, true);
        assertNotNull(result);
    }

    @Test
    void testExtractChunkImgForOpenApi_RetainImageId() {
        String content = "text ![img](https://agent_arts_knowledge_img_url/abc123) end";
        ChunkImageFormat result = ChunkUtils.extractChunkImgForOpenApi(content, ImageMaskPolicy.RETAIN_IMAGE_ID);
        assertNotNull(result);
        assertTrue(result.getChunkResult().contains("{KI|abc123}"));
        assertEquals(1, result.getImageIds().size());
        assertEquals("abc123", result.getImageIds().get(0));
    }

    @Test
    void testExtractChunkImgForOpenApi_RetainPlaceholder() {
        String content = "text ![img](https://agent_arts_knowledge_img_url/abc123) end";
        ChunkImageFormat result = ChunkUtils.extractChunkImgForOpenApi(content, ImageMaskPolicy.RETAIN_PLACEHOLDER);
        assertNotNull(result);
        assertTrue(result.getChunkResult().contains("{KI|0}"));
        assertEquals(1, result.getImageIds().size());
    }

    @Test
    void testExtractChunkImgForOpenApi_RemoveImage() {
        String content = "text ![img](https://agent_arts_knowledge_img_url/abc123) end";
        ChunkImageFormat result = ChunkUtils.extractChunkImgForOpenApi(content, ImageMaskPolicy.REMOVE_IMAGE);
        assertNotNull(result);
        assertFalse(result.getChunkResult().contains("![img]"));
        assertEquals(1, result.getImageIds().size());
    }

    @Test
    void testExtractChunkImgForOpenApi_BlankContent() {
        ChunkImageFormat result = ChunkUtils.extractChunkImgForOpenApi("", ImageMaskPolicy.RETAIN_IMAGE_ID);
        assertNotNull(result);
        assertEquals("", result.getChunkResult());
        assertTrue(result.getImageIds().isEmpty());
    }

    @Test
    void testExtractChunkImgForOpenApi_NullContent() {
        ChunkImageFormat result = ChunkUtils.extractChunkImgForOpenApi(null, ImageMaskPolicy.RETAIN_IMAGE_ID);
        assertNotNull(result);
        assertTrue(result.getImageIds().isEmpty());
    }

    @Test
    void testExtractChunkImgForOpenApi_NoImages() {
        ChunkImageFormat result = ChunkUtils.extractChunkImgForOpenApi("plain text", ImageMaskPolicy.RETAIN_IMAGE_ID);
        assertEquals("plain text", result.getChunkResult());
        assertTrue(result.getImageIds().isEmpty());
    }

    @Test
    void testExtractChunkImgForOpenApi_MultipleImages() {
        String content = "![img](https://agent_arts_knowledge_img_url/img1) and ![img](https://agent_arts_knowledge_img_url/img2)";
        ChunkImageFormat result = ChunkUtils.extractChunkImgForOpenApi(content, ImageMaskPolicy.RETAIN_PLACEHOLDER);
        assertEquals(2, result.getImageIds().size());
        assertTrue(result.getChunkResult().contains("{KI|0}"));
        assertTrue(result.getChunkResult().contains("{KI|1}"));
    }

    @Test
    void testGenerateChunkImageUriPath() {
        String path = ChunkUtils.generateChunkImageUriPath("proj-1", "kb-1", "img-1");
        assertEquals("/v1/proj-1/agent-manager/knowledges/kb-1/image/img-1", path);
    }
}
