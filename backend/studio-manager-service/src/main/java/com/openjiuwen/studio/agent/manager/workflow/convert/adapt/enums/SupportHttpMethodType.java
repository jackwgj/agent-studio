/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.enums;

public enum SupportHttpMethodType {
    GET,
    POST;

    public static boolean checkHttpMethod(String method) {
        for (SupportHttpMethodType supportHttpMethodType : SupportHttpMethodType.values()) {
            if (supportHttpMethodType.name().equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    public static SupportHttpMethodType fromValue(String method) {
        for (SupportHttpMethodType supportHttpMethodType : SupportHttpMethodType.values()) {
            if (supportHttpMethodType.name().equalsIgnoreCase(method)) {
                return supportHttpMethodType;
            }
        }
        return null;
    }
}
