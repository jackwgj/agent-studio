/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.domain;

/**
 * 任务执行的时候发现改任务需要被暂停，则抛出此异常
 *
 */
public class JobNeedBePausedException extends RuntimeException {
}
