/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.domain;

/**
 * 任务已暂停的异常，任务调度的时候发现这个任务已暂停，则抛出此异常
 *
 */
public class JobPausedException extends RuntimeException {
}
