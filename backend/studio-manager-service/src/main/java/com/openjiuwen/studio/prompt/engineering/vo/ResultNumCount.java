/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.vo;

import lombok.Data;

/**
 * 计算评估结果中的数据
 *
 */
@Data
public class ResultNumCount {
    private int score;

    private int token;

    private int correct;

    private double time;
}
