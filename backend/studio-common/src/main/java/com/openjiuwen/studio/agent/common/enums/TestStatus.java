/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 测试状态
 *
 */
@Getter
public enum TestStatus {
    SUCCESS(0),
    FAILED(1),
    UNKNOWN(2);

    private final Integer code;

    TestStatus(int code) {
        this.code = code;
    }

    public static TestStatus fromCode(Integer code) {
        // 数据库中若为null，默认状态为UNKNOWN
        if (code == null) {
            return UNKNOWN;
        }

        for (TestStatus status : values()) {
            if (Objects.equals(status.code, code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("unknown code of TestStatus: " + code);
    }
}
