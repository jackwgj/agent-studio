/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 功能描述
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class ErrorInfo {
    private String message;

    private String reason;

    private String suggestion;

    public ErrorInfo(String message, String reason, String suggestion) {
        this.message = message;
        this.reason = reason;
        this.suggestion = suggestion;
    }
}
