/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task;

/**
 * 线程命名
 *
 */
public class ThreadRenamer implements AutoCloseable {
    private final ThreadLocal<String> originalThreadName = ThreadLocal.withInitial(() -> Thread.currentThread().getName());

    public ThreadRenamer(String newName) {
        Thread.currentThread().setName(newName);
    }

    @Override
    public void close() {
        Thread.currentThread().setName(originalThreadName.get());
    }
}
