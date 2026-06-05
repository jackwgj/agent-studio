/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

import com.google.common.collect.Sets;

import java.util.Objects;
import java.util.Set;

/**
 * 评估任务状态
 *
 */
public enum EvaluationTaskStatus {
    /**
     * 等待中
     */
    pending,

    /**
     * 评估中
     */
    running,

    /**
     * 评估成功
     */
    success,

    /**
     * 评估失败
     */
    failed,

    /**
     * 重启
     */
    retrying,

    /**
     * 部分评估成功
     */
    partly_success,

    /**
     * 正在中止
     */
    pausing,

    /**
     * 已中止
     */
    paused;

    private static final Set<EvaluationTaskStatus> EXECUTING = Sets.newHashSet(pending, running, retrying, pausing);

    public static Set<EvaluationTaskStatus> executing() {
        return EXECUTING;
    }

    public static boolean isExecuting(EvaluationTaskStatus status) {
        return executing().contains(status);
    }

    private static final Set<EvaluationTaskStatus> EXPORT_STATUS = Sets.newHashSet(success, failed, paused);

    public static Set<EvaluationTaskStatus> exportStatus() {
        return EXPORT_STATUS;
    }

    public static boolean canExport(EvaluationTaskStatus status) {
        return exportStatus().contains(status);
    }

    /**
     * 判断数值是否属于当前状态枚举的值
     *
     * @return true - 属于 false - 不属于
     */
    public static boolean isInclude(String status) {
        boolean isInclude = false;
        for (EvaluationTaskStatus evaluationTaskStatus : EvaluationTaskStatus.values()) {
            if (Objects.equals(evaluationTaskStatus.name(), status)) {
                isInclude = true;
                break;
            }
        }
        return isInclude;
    }

    public static EvaluationTaskStatus getEvaluationTaskStatus(String status) {
        for (EvaluationTaskStatus evaluationTaskStatus : EvaluationTaskStatus.values()) {
            if (Objects.equals(evaluationTaskStatus.name(), status)) {
                return evaluationTaskStatus;
            }
        }
        return null;
    }
}
