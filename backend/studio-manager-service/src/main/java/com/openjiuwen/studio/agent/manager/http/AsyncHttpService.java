
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.http;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 服务中异步调用的方法集合，通过注解指定不同的线程池
 *
 */
@Service
public class AsyncHttpService {
    /**
     * 异步执行任务
     *
     * @param runnable 可执行的任务
     */
    @Async("runStream")
    public void callAgentStream(Runnable runnable) {
        runnable.run();
    }
}
