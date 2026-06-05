package com.openjiuwen.studio.agent.space.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 线程上下文
 */
public class ThreadLocalContext implements AutoCloseable {

    private static final TransmittableThreadLocal<ContextBean> SINGLE_INSTANCE = new TransmittableThreadLocal<>();

    public static ContextBean getInstance() {
        ContextBean instance = SINGLE_INSTANCE.get();
        if (null == instance) {
            synchronized (ContextBean.class) {
                if (null == SINGLE_INSTANCE.get()) {
                    instance = new ContextBean();
                    SINGLE_INSTANCE.set(instance);
                }
            }
        }
        return instance;
    }

    public static void clear() {
        SINGLE_INSTANCE.remove();
    }

    public TransmittableThreadLocal<ContextBean> getThreadLocal() {
        return SINGLE_INSTANCE;
    }

    @Override
    public void close() {
        SINGLE_INSTANCE.remove();
    }
}
