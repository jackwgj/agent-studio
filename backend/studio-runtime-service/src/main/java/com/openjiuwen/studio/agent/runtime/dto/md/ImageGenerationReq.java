/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.md;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.openjiuwen.studio.agent.common.dto.md.ModelInvokeBase;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImageGenerationReq extends ModelInvokeBase {
    @NotNull
    private String prompt;

    @NotNull
    private String size;

    @Pattern(regexp = "(b64_json|url)")
    @JsonProperty("response_format")
    private String responseFormat;

    private Integer seed;

    private Boolean watermark;

    private String image;

    @JsonProperty("guidance_scale")
    private Float guidanceScale;
}
