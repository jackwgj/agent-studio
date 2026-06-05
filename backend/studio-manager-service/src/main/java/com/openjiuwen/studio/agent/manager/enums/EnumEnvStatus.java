/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * @description 环境状态
 * @since 2025/9/19
 **/
public enum EnumEnvStatus {
    /**
     * 创建中
     */
    CREATING("CREATING"),

    /**
     * 失败
     */
    FAILED("FAILED"),

    /**
     * 更新中
     */
    UPDATING("UPDATING"),

    /**
     * 就绪
     */
    READY("READY"),

    /**
     * 删除中
     */
    DELETING("DELETING"),

    /**
     * 删除失败
     */
    DELETE_FAILED("DELETE_FAILED"),


    /**
     * 删除
     */
    DELETED("DELETED"),

    /**
     * 不存在
     */
    NOT_FOUND("NOT_FOUND"),

    ;

    private final String value;

    EnumEnvStatus(String value) {
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
     * 字符串转枚举
     *
     */
    public static EnumEnvStatus fromStatus(String value) {
        for (EnumEnvStatus status : EnumEnvStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}

