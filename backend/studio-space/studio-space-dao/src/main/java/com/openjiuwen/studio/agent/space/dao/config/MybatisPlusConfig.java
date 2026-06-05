/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * mybatis拓展类，实现批量插入能力
 */
@Configuration
public class MybatisPlusConfig {
    @Bean
    public EasySqlInjector easySqlInjector() {
        return new EasySqlInjector();
    }

    // 自定义 SQL 注入器
    public class EasySqlInjector extends DefaultSqlInjector {
        @Override
        public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
            List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
            // 添加自定义方法
            methodList.add(new InsertBatchSomeColumn());
            methodList.add(new AlwaysUpdateSomeColumnById());
            return methodList;
        }
    }
}