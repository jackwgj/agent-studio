/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.prompt.engineering.enums.v2.ExecTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型或者agent 的id
     */
    @JsonProperty("model")
    private String model;

    /**
     * 执行体类型
     */
    @JsonProperty("exec_type")
    private ExecTypeEnum execType;


    /**
     * 超参数
     */
    @JsonProperty("top_p")
    private Double topP;

    /**
     * 超参数
     */
    @JsonProperty("temperature")
    private Double temperature;

    /**
     * 超参数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;


}
