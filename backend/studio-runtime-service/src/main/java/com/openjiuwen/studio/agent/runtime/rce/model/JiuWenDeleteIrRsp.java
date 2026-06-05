/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 九问删除会话IR实例接口的响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiuWenDeleteIrRsp {
    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;
}
