/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * FileType
 *
 */

public enum FileType {
    /*
     * json文件
     */
    JSON("json"),

    /*
     * 图片
     */
    IMAGE("image"),

    /**
     * 压缩包
     */
    ZIP("zip");

    private final String value;

    FileType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
