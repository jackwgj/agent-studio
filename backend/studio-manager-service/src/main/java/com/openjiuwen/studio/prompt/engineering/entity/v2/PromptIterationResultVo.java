/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 提示词迭代结果信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromptIterationResultVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("current_best_prompt")
    private String currentBestPrompt;

    @JsonProperty("iteration_info_list")
    private List<IterationInfo> iterationInfoList;

}
