/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

public enum FileStatusEnum {

    RUNNING("1"),
    PENDING("0"),
    SUCCESS("3"),
    ERROR("4");

    private String value;

    FileStatusEnum(String value) {
        this.value = value;
    }

    public static String convert(String status) {
        for (FileStatusEnum value : FileStatusEnum.values()) {
            if (value.value.equals(status)) {
                return value.name();
            }
        }
        return null;
    }

    public static String converse_convert(String status) {
        for (FileStatusEnum item : FileStatusEnum.values()) {
            if (item.name().equals(status)) {
                return item.value;
            }
        }
        return null;
    }
}
