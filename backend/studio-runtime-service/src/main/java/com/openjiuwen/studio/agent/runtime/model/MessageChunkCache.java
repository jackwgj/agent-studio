/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class MessageChunkCache {
    private final static int MAX_CHUNK_COUNT = 1000;

    private boolean hasDrained = false;

    private StringBuilder contentBuilder = new StringBuilder();

    private int chunkCount = 0;

    public String drain() {
        String content = contentBuilder.toString();
        contentBuilder = new StringBuilder();
        chunkCount = 0;
        hasDrained = true;
        return content;
    }

    public MessageChunkCache addChunk(String chunkContent) {
        contentBuilder.append(chunkContent == null ? "" : chunkContent);
        chunkCount += 1;
        return this;
    }

    public boolean isFull() {
        return chunkCount >= MAX_CHUNK_COUNT;
    }

    public boolean containsStartChunk() {
        return !hasDrained;
    }
}
