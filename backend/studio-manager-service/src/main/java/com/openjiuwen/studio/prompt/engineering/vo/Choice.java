/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * playground调优，大模型侧choices参数接收体
 *
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Choice {
    private Message message;

    private Integer index;
}
