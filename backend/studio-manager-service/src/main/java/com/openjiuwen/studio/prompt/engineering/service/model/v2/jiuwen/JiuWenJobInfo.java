/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiuWenJobInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id;

    @JsonProperty("assistant_model")
    private String assistantModel;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("desc")
    private String desc;

    @JsonProperty("name")
    private String name;

    @JsonProperty("num_iter")
    private int numIter;

    @JsonProperty("optimized_model")
    private String optimizedModel;

}
