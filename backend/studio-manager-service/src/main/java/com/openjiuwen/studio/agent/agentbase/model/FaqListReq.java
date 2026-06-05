/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量删除FAQ请求体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FaqListReq {
    @JsonProperty("repo_id")
    private String repoId;

    @JsonProperty("faq_ids")
    private List<String> faqIds;
}
