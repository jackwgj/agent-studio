/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.constant;

/**
 * 布尔查询解析服务常量
 */
public final class BoolQueryConstants {

    // 私有构造函数，防止实例化
    private BoolQueryConstants() {}

    // Lucene 特殊字符
    public static final String LUCENE_SPECIAL_CHARS = "+-=&|!(){}[]^\"~*?:\\/";

    // 查询字符串模板
    public static final String NOT_OPERATOR_TEMPLATE = "(NOT %s)";
    public static final String GROUP_OPERATOR_TEMPLATE = "(%s)";
    public static final String SPACE = " ";
}
