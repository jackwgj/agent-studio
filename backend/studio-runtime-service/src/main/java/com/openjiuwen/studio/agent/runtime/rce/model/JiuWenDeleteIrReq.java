/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 九问删除会话IR实例接口的请求体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiuWenDeleteIrReq {
    /**
     * 会话id
     */
    private String conversationId;

    /**
     * ir路径
     */
    private String irPath;

}
