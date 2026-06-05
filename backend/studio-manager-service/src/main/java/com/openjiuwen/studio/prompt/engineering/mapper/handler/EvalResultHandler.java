/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.prompt.engineering.dto.PeEvalResult;

public class EvalResultHandler extends AbstractJsonTypeHandler<PeEvalResult> {
    private static final TypeReference<PeEvalResult> TYPE_REF = new TypeReference<PeEvalResult>() {};

    @Override
    protected TypeReference<PeEvalResult> getTypeReference() {
        return TYPE_REF;
    }
}
