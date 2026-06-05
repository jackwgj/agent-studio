/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * @description mcp 安装方式
 * @since 2025/5/9
 **/
public enum EnumOrgType {
    SSE("SSE"),

    NPX("NPX"),

    UVX("UVX"),

    STREAMABLE_HTTP("streamable_http");

    private String value;

    EnumOrgType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据字符串值获取枚举
     * @param value 字符串值
     * @return 对应的枚举
     */
    public static EnumOrgType fromValue(String value) {
        for (EnumOrgType type : EnumOrgType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant " + EnumOrgType.class.getName() + "." + value);
    }
}

