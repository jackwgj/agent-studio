/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 生成高智能代码体基本信息响应体对象
 *
 */
@Data
public class GenerateCodeAgentInfoRsp {
    private String code;

    private String message;

    private CodeAgentBaseInfo data;

    @Data
    public static class CodeAgentBaseInfo {
        @JsonProperty("agent_id")
        private String agentId;

        private String name;

        private String description;
    }
}
