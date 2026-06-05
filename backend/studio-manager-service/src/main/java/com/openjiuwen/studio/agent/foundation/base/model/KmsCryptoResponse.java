/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.base.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorDetail;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KmsCryptoResponse {

    private String plainText;

    private String cipherText;

    private boolean oldDataFlag;

    private ErrorDetail error;
}
