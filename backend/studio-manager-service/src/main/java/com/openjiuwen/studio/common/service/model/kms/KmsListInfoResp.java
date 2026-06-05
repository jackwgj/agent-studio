/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.common.service.model.kms;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class KmsListInfoResp {

    @JsonProperty("Customized_Keys")
    @Builder.Default
    private List<KmsDetail> customizedKeys = new ArrayList<>();

    @JsonProperty("Default_Key")
    @Builder.Default
    private KmsDetail defaultKey = KmsDetail.builder().build();

    @JsonProperty("AgentBuilder_Key")
    @Builder.Default
    private KmsDetail agentBuilderKey = KmsDetail.builder().build();

    @JsonProperty("Key_state")
    private boolean kmsState;
}
