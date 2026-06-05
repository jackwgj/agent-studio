/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.enums;

/**
 * 文件上传场景枚举
 */
public enum AgentBuilderUploadScene {
    USER_UPLOAD(0),
    RUNTIME_UPLOAD(1),
    ;

    private final int scene;

    AgentBuilderUploadScene(int scene) {
        this.scene = scene;
    }
}