/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.common.service.model.kms;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KmsDetail {

    @JsonProperty("key_id")
    private String keyId;

    @JsonProperty("key_alias")
    private String keyAlias;
}
