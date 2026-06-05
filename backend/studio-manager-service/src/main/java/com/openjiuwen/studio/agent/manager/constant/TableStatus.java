/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.constant;

/**
 * @ClassName : TableStatus
 * @Description :数据表状态枚举类
 * @Date : Created in 2024/4/8 19:35
 * @Version : V1.0
 */
public enum TableStatus {

    CREATED("created"),

    RELEASED("released"),

    DELETED("deleted");

    private String value;

    TableStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
