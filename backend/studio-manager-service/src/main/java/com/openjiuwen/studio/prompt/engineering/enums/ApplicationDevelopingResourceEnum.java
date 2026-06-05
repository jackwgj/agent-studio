/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

/**
 * 功能描述 应用开发资源项枚举
 *
 */
public enum ApplicationDevelopingResourceEnum {

    /**
     * 基础版
     */
    BASIC("AgentBuilder_Kits_Application_Developing_NLP_Basic"),

    /**
     * 高级版
     */
    ADVANCED("AgentBuilder_Kits_Application_Developing_NLP_Advanced");

    private final String resourceSpecCode;

    ApplicationDevelopingResourceEnum(String resourceSpecCode) {
        this.resourceSpecCode = resourceSpecCode;
    }

    public String getResourceSpecCode() {
        return resourceSpecCode;
    }
}
