/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core;

/**
 * 租户清理管道任务接口
 * <p>
 * 所有租户清理的管道任务都必须实现此接口
 *
 * @since 2025-12-20
 */
public interface TenantCleanPipelineTask {

    /**
     * 执行业务逻辑
     *
     * @param context 管道上下文
     */
    void execute(TenantCleanPipelineContext context);

    /**
     * 获取任务名称（用于日志和调试）
     *
     * @return 任务名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取任务执行顺序
     * 数值越小，执行优先级越高
     *
     * @return 顺序值
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * 检查任务是否应该执行
     * 默认情况下总是执行，子类可以重写此方法实现条件执行
     *
     * @param context 管道上下文
     * @return 是否应该执行
     */
    default boolean shouldExecute(TenantCleanPipelineContext context) {
        return true;
    }

    /**
     * 执行失败时的处理逻辑
     * 默认只是记录日志，子类可以重写实现自定义处理
     *
     * @param context 管道上下文
     * @param exception 异常信息
     */
    default void onFailed(TenantCleanPipelineContext context, Exception exception) {
        // 子类可以重写此方法实现自定义失败处理
    }
}