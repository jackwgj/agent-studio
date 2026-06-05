/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

/**
 * 对接语言模型枚举类
 *
 */
public enum ModelType {
    /**
     * 盘古大模型
     */
    AGENT_BUILDER("AGENT_BUILDER");

    private String type;

    ModelType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ResponseCode{" + ", type='" + type + '\'' + '}';
    }
}
