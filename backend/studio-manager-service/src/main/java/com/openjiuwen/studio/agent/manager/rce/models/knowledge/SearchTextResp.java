/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.ChatReferenceInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Css搜索知识库响应实体类
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchTextResp {
    private Integer total;

    @JsonProperty("doc_list")
    private List<ChatReferenceInfo> docList;
}
