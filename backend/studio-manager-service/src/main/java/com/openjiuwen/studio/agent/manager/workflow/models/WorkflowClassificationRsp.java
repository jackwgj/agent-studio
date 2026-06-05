/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 意图识别节点分类配置
 * 样例：{{"keywords": ["bad service", "slow", "food", "tip", "terrible", "waitresses"],"category_id":
 * "f6ff5bc3-aca0-4e4a-8627-e760d0aca78f","category_name": "Experience"}}
 *
 */
@Data
public class WorkflowClassificationRsp {

    private List<String> keywords;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("category_name")
    private String categoryName;
}
