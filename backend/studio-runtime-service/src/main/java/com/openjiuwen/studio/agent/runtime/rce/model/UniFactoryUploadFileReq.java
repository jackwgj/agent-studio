/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.core.io.Resource;

/**
 * UniFactory上传文件响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniFactoryUploadFileReq {
    /**
     * 文件资源
     */
    private Resource file;

    /**
     * 是否使用ocr方式解析文档
     */
    private Boolean ocr;

    /**
     * 是否强制使用ocr增强
     */
    @JsonProperty("ocr_enabled")
    private Boolean ocrEnabled;

    /**
     * 是否解析图片
     */
    @JsonProperty("image_enabled")
    private Boolean imageEnabled;

    /**
     * 图片处理方式-TEXT:转文本,IMAGE-保留图片
     */
    @JsonProperty("image_conf")
    private String imageConf;

    /**
     * 拆分模式-默认AUTO-自动选择拆分模式
     */
    @JsonProperty("split_mode")
    private String splitMode;

    /**
     * 是否解析目录
     */
    @Builder.Default
    @JsonProperty("catalog_enabled")
    private boolean catalogEnabled = true;
}
