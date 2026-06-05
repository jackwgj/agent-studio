/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for MessageChunkCache class
 */
public class MessageChunkCacheTest {

    @Test
    public void test_initial_state() {
        MessageChunkCache cache = new MessageChunkCache();

        assertTrue(cache.containsStartChunk());
        assertNotNull(cache.getContentBuilder());
        assertEquals(0, cache.getChunkCount());
        assertEquals(0, cache.getContentBuilder().length());
    }

    @Test
    public void test_add_chunk() {
        MessageChunkCache cache = new MessageChunkCache();

        cache.addChunk("Hello");
        cache.addChunk(" ");
        cache.addChunk("World");

        assertEquals(3, cache.getChunkCount());
        assertEquals("Hello World", cache.getContentBuilder().toString());
    }

    @Test
    public void test_is_full() {
        MessageChunkCache cache = new MessageChunkCache();

        // Fill up to 999 chunks - should not be full
        for (int i = 0; i < 999; i++) {
            cache.addChunk("chunk");
            assertFalse(cache.isFull());
        }

        // Add 1000th chunk - should be full
        cache.addChunk("chunk");
        assertTrue(cache.isFull());

        // Add another chunk - should still be full
        cache.addChunk("chunk");
        assertTrue(cache.isFull());
        assertEquals(1001, cache.getChunkCount());
    }

    @Test
    public void test_drain() {
        MessageChunkCache cache = new MessageChunkCache();

        // Add some content
        cache.addChunk("Hello");
        cache.addChunk(" ");
        cache.addChunk("World");

        // drain
        String content = cache.drain();

        assertEquals("Hello World", content);
        assertFalse(cache.containsStartChunk());
        assertEquals(0, cache.getChunkCount());
        assertEquals(0, cache.getContentBuilder().length());

        // Get and clear again should return empty
        String content2 = cache.drain();
        assertEquals("", content2);
        assertFalse(cache.containsStartChunk());
        assertEquals(0, cache.getChunkCount());
    }

    @Test
    public void test_add_chunk_after_drain() {
        MessageChunkCache cache = new MessageChunkCache();

        // Add initial content
        cache.addChunk("Initial");
        assertEquals("Initial", cache.getContentBuilder().toString());

        // drain
        cache.drain();

        // Add new content after clear
        cache.addChunk("New");
        cache.addChunk("Content");

        assertEquals("NewContent", cache.getContentBuilder().toString());
        assertEquals(2, cache.getChunkCount());
    }

    @Test
    public void test_chain_setter() {
        MessageChunkCache cache = new MessageChunkCache();

        MessageChunkCache result = cache.addChunk("Test");
        assertSame(cache, result); // Should return same instance
    }

    @Test
    public void test_with_empty_string_chunk() {
        MessageChunkCache cache = new MessageChunkCache();

        // Empty strings should not break the cache
        cache.addChunk("");
        cache.addChunk("");
        cache.addChunk("Non-empty");

        assertEquals("Non-empty", cache.getContentBuilder().toString());
        assertEquals(3, cache.getChunkCount());
    }
}
