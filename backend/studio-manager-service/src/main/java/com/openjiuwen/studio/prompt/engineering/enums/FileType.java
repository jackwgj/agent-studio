/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

import lombok.Getter;

/**
 * 文件类型枚举
 */
@Getter
public enum FileType {
    FILE_TYPE_EXCEL("excel"),

    IMAGE("image"),

    JSON("json");

    FileType(String fileType) {
        this.fileType = fileType;
    }

    private final String fileType;

}
