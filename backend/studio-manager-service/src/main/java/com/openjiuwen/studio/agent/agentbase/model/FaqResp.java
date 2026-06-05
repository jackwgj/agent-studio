/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建FAQ响应体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FaqResp {
    @JsonProperty("faq_id")
    private String faqId;
}
