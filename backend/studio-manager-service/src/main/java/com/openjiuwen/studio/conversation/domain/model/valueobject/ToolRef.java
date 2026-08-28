/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.domain.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具引用值对象（一行工具调用 = 一次引用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolRef {
    /** 一次具体工具调用的业务 ID。 */
    private String toolId;

    /** 工具名称，可重复，不承担调用唯一性。 */
    private String toolName;

    /** 工具调用请求参数 JSON。 */
    private String args;
}
