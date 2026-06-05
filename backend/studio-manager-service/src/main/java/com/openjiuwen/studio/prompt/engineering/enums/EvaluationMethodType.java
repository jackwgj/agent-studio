/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 评估方法枚举
 *
 */
@Getter
public enum EvaluationMethodType {
    /**
     * 准确匹配
     */
    MATCHING("matching"),
    /**
     * 正则匹配
     */
    REGULAR_MATCHING("regular_matching"),
    /**
     * 模型评估
     */
    MODEL("model"),
    /**
     * 相似度
     */
    SIMILARITY("similarity");

    private String method;

    EvaluationMethodType(String method) {
        this.method = method;
    }

    /**
     * 根据methodKey判断数值是否属于评估方法枚举的值
     *
     * @return true - 属于 false - 不属于
     */
    public static boolean isInclude(String method) {
        boolean isInclude = false;
        for (EvaluationMethodType type : EvaluationMethodType.values()) {
            if (Objects.equals(type.getMethod(), method)) {
                isInclude = true;
                break;
            }
        }
        return isInclude;
    }

    public static EvaluationMethodType getEvaluationMethodType(String method) {
        for (EvaluationMethodType type : EvaluationMethodType.values()) {
            if (Objects.equals(type.getMethod(), method)) {
                return type;
            }
        }
        return null;
    }
}
