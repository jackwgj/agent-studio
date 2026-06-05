/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * md文件导入请求
 */
@Data
@Accessors(chain = true)
public class KooPageMdImportReq {
    /**
     * 导入后文档标题
     */
    @NotBlank
    @JsonProperty("title")
    private String title;

    /**
     * 文档内容
     */
    @NotBlank
    @JsonProperty("content")
    private String content;
}
