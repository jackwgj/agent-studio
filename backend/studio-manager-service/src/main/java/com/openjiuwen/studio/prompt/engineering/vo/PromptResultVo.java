/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * playground调优，大模型侧结果就收体
 *
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromptResultVo {
    private String id;

    private Long created;

    private List<Choice> choices;

    private Usage usage;

    private double time;
}
