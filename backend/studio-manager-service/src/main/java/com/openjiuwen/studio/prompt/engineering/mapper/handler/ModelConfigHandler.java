/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;

public class ModelConfigHandler extends AbstractJsonTypeHandler<ModelConfig> {

    private static final TypeReference<ModelConfig> TYPE_REF = new TypeReference<ModelConfig>() {
    };

    @Override
    protected TypeReference<ModelConfig> getTypeReference() {
        return TYPE_REF;
    }
}
