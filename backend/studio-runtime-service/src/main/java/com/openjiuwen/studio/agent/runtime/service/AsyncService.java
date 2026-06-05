/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 */
@Service
public class AsyncService {
    /**
     * 异步执行任务
     *
     * @param runnable 可执行任务
     */
    @Async("asyncExecutor")
    public void callPromptBuilder(Runnable runnable) {
        runnable.run();
    }
}
