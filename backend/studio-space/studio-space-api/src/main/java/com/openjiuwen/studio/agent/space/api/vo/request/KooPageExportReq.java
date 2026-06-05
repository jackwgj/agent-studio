/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * kooPage导出请求体
 */
@Data
@Accessors(chain = true)
public class KooPageExportReq {
    /**
     * kooPage对应的文档id
     */
    @NotBlank
    @JsonProperty("document_id")
    private String documentId;

    /**
     * 文档类型
     */
    @NotBlank
    @JsonProperty("export_type")
    private String exportType;
}
