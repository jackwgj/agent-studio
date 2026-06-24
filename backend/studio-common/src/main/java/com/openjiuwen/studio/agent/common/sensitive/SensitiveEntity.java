/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.sensitive;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 敏感词配置实体类
 *
 */
@Getter
@Setter
public class SensitiveEntity {
    @JsonProperty("enabled")
    @JSONField(name = "enabled")
    private boolean enabled;

    WordsWrapper filter;

    List<WordsWrapper> replace;

    List<WordsWrapper> reply;

    /**
     * 敏感词包装类
     * 
     */
    @Getter
    @Setter
    public static class WordsWrapper {
        String keywords;

        String replace;

        @JsonProperty("input_enable")
        @JSONField(name = "input_enable")
        private boolean inputEnable;

        @JsonProperty("input_text")
        @JSONField(name = "input_text")
        String inputReplyText;

        @JsonProperty("output_enable")
        @JSONField(name = "output_enable")
        private boolean outputEnable;

        @JsonProperty("output_text")
        @JSONField(name = "output_text")
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
