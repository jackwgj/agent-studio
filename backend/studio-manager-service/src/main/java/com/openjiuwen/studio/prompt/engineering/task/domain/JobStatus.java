/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.domain;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 维护任务状态的枚举类
 *
 */
public enum JobStatus {
    pending,
    running,
    retrying,
    failed,
    partlysuccess,
    success,
    pausing,
    paused;

    private final static Set<JobStatus> CAN_STOP_MANUAL = Sets.newHashSet(running);

    private final static Set<JobStatus> CAN_EXECUTE = Sets.newHashSet(pending, retrying);

    private final static Set<JobStatus> EXECUTING = Sets.newHashSet(running);

    private final static Set<JobStatus> CAN_DELETE = Sets.newHashSet(failed, partlysuccess, success, paused);

    /**
     * 返回可以被执行的任务状态
     *
     * @return
     */
    public static Set<JobStatus> canExecute() {
        return CAN_EXECUTE;
    }

    public static Set<JobStatus> executing() {
        return EXECUTING;
    }

    /**
     * 可以手动停止的状态
     *
     * @return 返回可以停止的状态
     */
    public static Set<JobStatus> canStopManual() {
        return CAN_STOP_MANUAL;
    }

    /**
     * 这些状态是可以删除的
     *
     * @return 返回可以删除的状态
     */
    public static Set<JobStatus> canDelete() {
        return CAN_DELETE;
    }
}
