/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.prompt.engineering.dto.SmallPromptDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * SmallPrompt
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmallPrompt implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Builder.Default
    private String id = null;

    @JsonProperty("manual_score")
    @Builder.Default
    private Integer manualScore = null;

    @JsonProperty("name")
    @Builder.Default
    private String name = null;

    public SmallPrompt convertToSmallPrompt(SmallPromptDto smallPromptDTO) {
        SmallPromptDTOToSmallPromptConverter toSmallPromptConverter = new SmallPromptDTOToSmallPromptConverter();
        return toSmallPromptConverter.convert(smallPromptDTO);
    }

    private static class SmallPromptDTOToSmallPromptConverter implements FormConvert<SmallPromptDto, SmallPrompt> {
        @Override
        public SmallPrompt convert(SmallPromptDto smallPromptDTO) {
            return SmallPrompt.builder().id(smallPromptDTO.getId()).manualScore(smallPromptDTO.getManualScore())
                    .name(smallPromptDTO.getName()).build();
        }
    }
}
