/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.md;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.openjiuwen.studio.agent.common.dto.md.ModelInvokeBase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @Description RankDocumentsRequest
 * @Since 2024/10/17
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RankDocumentsRequest extends ModelInvokeBase {

    @Size(max = 4000)
    @NotNull(message = "Parameter 'query' must be not null, please check and try again later!")
    private String query;

    @Size(max = 10,message = "Parameter 'docs' must not contain more than 10 elements, please check and try again later!")
    @NotNull(message = "Parameter 'docs' must be not null, please check and try again later!")
    private List<String> docs;

    @JsonProperty("top_n")
    @JSONField(name = "top_n")
    @NotNull(message = "Parameter 'docs' must be not null, please check and try again later!")
    @Max(512)
    @Min(1)
    private Integer topN;
}
