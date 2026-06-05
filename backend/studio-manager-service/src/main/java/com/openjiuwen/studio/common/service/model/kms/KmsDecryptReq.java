/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.common.service.model.kms;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KmsDecryptReq {

    @JsonProperty("cipher_text")
    private String cipherText;
}
