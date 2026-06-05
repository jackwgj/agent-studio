/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

import com.google.common.collect.Sets;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;


/**
 * 评估任务状态
 *
 */
@Getter
public enum OptimizationTaskStatus {
    /**
     * 等待中
     */
    PENDING("pending"),

    /**
     * 优化中
     */
    RUNNING("running"),

    /**
     * 暂停
     */
    PAUSED("paused"),

    /**
     * 优化成功
     */
    SUCCESS("success"),

    /**
     * 优化失败
     */
    FAILED("failed");

    private static final Set<OptimizationTaskStatus> EXECUTING = Sets.newHashSet(RUNNING);

    private final String code;

    OptimizationTaskStatus(String code) {
        this.code = code;
    }

    public static Set<OptimizationTaskStatus> executing() {
        return EXECUTING;
    }

    /**
     * 状态映射
     *
     * @param status
     * @return
     */
    public static OptimizationTaskStatus getByCode(String status) {
        for (OptimizationTaskStatus evaluationTaskStatus : OptimizationTaskStatus.values()) {
            if (evaluationTaskStatus.getCode().equals(status)) {
                return evaluationTaskStatus;
            }
        }
        throw new AgentStudioException(StudioError.OPTIMIZATION_TASK_STATUS_IS_NOT_EXIST, Collections.singletonList(
            status));
    }
}
