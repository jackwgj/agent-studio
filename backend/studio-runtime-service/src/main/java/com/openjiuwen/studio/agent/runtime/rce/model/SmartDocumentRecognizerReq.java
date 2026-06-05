/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OCR智能文档识别请求体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmartDocumentRecognizerReq {
    /**
     * 图片的base64编码
     */
    private String data;

    /**
     * 语种选择，未传入该参数时默认为中英文识别模式
     */
    private String language;

    /**
     * 是否进行表格识别。此处表格特指逻辑表格
     */
    private boolean table;

    /**
     * 单朝向模式开关
     */
    @JsonProperty("single_orientation_mode")
    private boolean singleOrientationMode;

    /**
     * 是否进行印章擦除
     */
    @JsonProperty("erase_seal")
    private boolean eraseSeal;
}
