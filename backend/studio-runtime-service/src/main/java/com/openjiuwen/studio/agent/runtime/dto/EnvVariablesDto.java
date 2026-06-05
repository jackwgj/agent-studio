/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Data;

import java.util.List;

/**
 * 环境变量实体类
 *
 */
@Data
@SuppressWarnings("checkstyle: all")
public class EnvVariablesDto {
    /**
     * 类型
     */
    public static final TypeReference<List<EnvVariablesDto>> TYPE_REFERENCE = new TypeReference<>() {};

    private String name;

    private EnvVariableValue value;

    /**
     * 环境变量值
     *
     */
    @Data
    public static class EnvVariableValue {
        private String type;

        private String content;

        private boolean secret;
    }
}
