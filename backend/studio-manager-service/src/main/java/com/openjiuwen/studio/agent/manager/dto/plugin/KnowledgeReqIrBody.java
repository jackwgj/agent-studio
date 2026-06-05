/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeReqIrBody {

    @Builder.Default
    private String method = CommonConstant.ControllerConstants.IR_ARG_BODY_METHOD;

    @Builder.Default
    private boolean required = true;

    @Builder.Default
    private String validateType = "CHAR";

    @Builder.Default
    private boolean validated = false;

    private String name;

    private  String description ;

    private String type;

    private boolean visible;

    @JsonProperty("default_value")
    private Object defaultValue;

    private List<KnowledgeReqIrBody> schema;

}
