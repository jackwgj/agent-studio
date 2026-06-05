/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * sse发送的信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmitterInfo {
    private String content;

    private String name;
}
