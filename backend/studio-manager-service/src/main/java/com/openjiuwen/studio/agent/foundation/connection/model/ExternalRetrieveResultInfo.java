/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.model;

import lombok.Data;

import java.util.List;

/**
 * 知识检索结果
 *
 * @since 2025-08-22
 */
@Data
public class ExternalRetrieveResultInfo {

    private String knowledgeBaseId;

    private String title;

    private String content;

    private Float score;

    private String fileId;

    private String chunkId;

    // 切片中的图片id列表
    private List<String> imageIds;

    // 切片中的图片文件网络地址列表
    private List<String> imagePaths;

}
