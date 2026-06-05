/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 查询IAM用户详情
 *
 */
@Data
@Accessors(chain = true)
public class IamUserInfoLink {

    /**
     * 资源链接地址。
     */
    @JsonProperty("self")
    private String self;

    /**
     * 前一邻接资源链接地址，不存在时为null。
     */
    @JsonProperty("previous")
    private String previous;

    /**
     * 后一邻接资源链接地址，不存在时为null。
     */
    @JsonProperty("next")
    private String next;
}
