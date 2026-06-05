/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 功能描述
 *
 */
@Configuration
public class MybatisPlusConfig {
    @Value("${mybatis-plus.global-config.db-config.db-type}")
    private DbType dbType;

    /**
     * mybatisPlusInterceptor
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 1. 分页拦截器（必选）
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(dbType);
        interceptor.addInnerInterceptor(paginationInterceptor);
        // 2. 乐观锁拦截器（可选，如果使用@Version注解）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 3. 防止全表更新/删除拦截器（强烈推荐生产环境开启）
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }
}
