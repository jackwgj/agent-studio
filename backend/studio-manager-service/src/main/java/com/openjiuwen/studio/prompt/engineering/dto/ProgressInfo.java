/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板自优化进度
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressInfo {

    private float progressRate;

    private String bestPrompt;

    private String errorMsg;

    private String status;

}
