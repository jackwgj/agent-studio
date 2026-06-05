/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.sensitive;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 敏感词配置实体类
 *
 */
@Data
public class SensitiveEntity {
    @JsonProperty("enabled")
    boolean isEnabled;

    WordsWrapper filter;

    List<WordsWrapper> replace;

    List<WordsWrapper> reply;

    /**
     * 敏感词包装类
     *
     */
    @Data
    public static class WordsWrapper {
        String keywords;

        String replace;

        @JsonProperty("input_enable")
        boolean isInputEnable;

        @JsonProperty("input_text")
        String inputReplyText;

        @JsonProperty("output_enable")
        boolean isOutputEnable;

        @JsonProperty("output_text")
        String outputReplyText;

        public List<String> getKeywordsList() {
            if (StringUtils.isBlank(keywords)) {
                return new ArrayList<>();
            }
            return Stream.of(keywords.split(","))
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toList());
        }
    }
}
