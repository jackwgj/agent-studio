/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 将内部检索接口包装过图片的返回内容提取成占位符和图片列表
 */
@Data
@Builder
public class ChunkImageFormat {

    private String knowledgeBaseId;

    /**
     * image-id和redis-key的映射关系
     */
    private Map<String, String> imageAccessMaps;

    /**
     * 检索结果中的imageId
     */
    private List<String> imageIds;

    /**
     * 按规则提取图片后的检索内容
     */
    private String chunkResult;
}