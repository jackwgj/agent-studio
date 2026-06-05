/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.common.service.model.kms;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KmsDecryptResp {

    @JsonProperty("plain_text")
    private String plainText;

    private ErrorDetail error;

    @JsonProperty("old_data_flag")
    private boolean oldDataFlag;
}
