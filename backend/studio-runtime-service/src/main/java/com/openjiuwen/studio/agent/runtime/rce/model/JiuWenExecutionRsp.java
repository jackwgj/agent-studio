/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 九问执行IR接口响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiuWenExecutionRsp {
    /**
     * 会话id
     */
    private String conversationId;

    /**
     * 会话响应内容
     */
    private ExecutionData data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExecutionData {
        /**
         * Agent回答
         */
        private String answer;
    }
}
