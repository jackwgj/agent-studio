/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 生成高智能代码体基本信息请求体
 *
 */
@Data
@Builder
public class GenerateCodeAgentInfoReq {

    @JsonProperty("user_input")
    private String userInput;
}
