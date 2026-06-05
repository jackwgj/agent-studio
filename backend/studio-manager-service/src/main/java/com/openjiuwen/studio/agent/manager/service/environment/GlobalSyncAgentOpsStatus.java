/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.environment;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * 全局待同步数据队列，用户异步同步数据信息
 *
 */
@Slf4j
public class GlobalSyncAgentOpsStatus<T> {
    private static volatile GlobalSyncAgentOpsStatus<?> instance;

    /**
     * 线程安全的队列
     */
    private final ConcurrentLinkedQueue<T> queue;

    /**
     * 处理数据的消费者
     */
    private Consumer<T> dataProcessor;

    /**
     * 是否运行
     */
    private final boolean isRunning;

    /**
     * 私有构造函数，防止外部实例化
     *
     */
    private GlobalSyncAgentOpsStatus() {
        queue = new ConcurrentLinkedQueue<>();
        isRunning = false;
    }

    /**
     * 同步实例
     */
    private static final Object INSTANCE_LOCK = new Object();

    /**
     * 获取单例实例
     *
     */
    @SuppressWarnings("unchecked")
    public static <T> GlobalSyncAgentOpsStatus<T> getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    instance = new GlobalSyncAgentOpsStatus<>();
                }
            }
        }
        return (GlobalSyncAgentOpsStatus<T>) instance;
    }

    /**
     * 处理方法
     *
     */
    public void setDataProcessor(Consumer<T> processor) {
        this.dataProcessor = processor;
    }

    /**
     * 启动线程
     *
     */
    public void start() {
        if (isRunning || dataProcessor == null) {
            return;
        }

        // 队列空时休眠，减少CPU消耗
        Thread processThread = new Thread(() -> {
            while (true) {
                try {
                    T data = queue.poll();
                    if (data != null) {
                        dataProcessor.accept(data);
                    } else {
                        // 队列空时休眠，减少CPU消耗
                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    log.error("Sync data error: {}", e.getMessage());
                }
            }
        }, "Sync-Env-STATE-Queue-Processor");
        processThread.start();
    }

    /**
     * 添加数据到队列
     *
     */
    public void addData(T data) {
        if (data != null && !this.contains(data)) {
            queue.add(data);
        }
    }

    public boolean contains(T data) {
        return queue.contains(data);
    }

    /**
     * 获取队列大小
     *
     */
    public int size() {
        return queue.size();
    }

    /**
     * 清空队列
     *
     */
    public void clear() {
        queue.clear();
    }
}
