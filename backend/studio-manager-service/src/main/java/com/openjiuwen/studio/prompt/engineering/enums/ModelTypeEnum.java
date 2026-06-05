/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.Getter;

import java.util.Collections;
import java.util.Objects;


/**
 * 模型类型
 */
@Getter
public enum ModelTypeEnum {

    /**
     * pgt_multimodal_llm
     */
    PGT_MULTIMODAL_LLM("pgt_multimodal_llm"),


    /**
     * xiaoyi_llm
     */
    XIAOYI_LLM("xiaoyi_llm");

    private final String code;

    ModelTypeEnum(String code) {
        this.code = code;
    }

    /**
     * 模型类型
     *
     * @param modelType
     * @return
     */
    public static String getModelType(String modelType) {
        for (ModelTypeEnum evaluationTaskStatus : ModelTypeEnum.values()) {
            if (Objects.equals(evaluationTaskStatus.getCode(), modelType)) {
                return evaluationTaskStatus.getCode();
            }
        }
        throw new AgentStudioException(StudioError.MODEL_TYPE_IS_NOT_EXIST, Collections.singletonList(modelType));
    }

    public String getCode() {
        return code;
    }
}
