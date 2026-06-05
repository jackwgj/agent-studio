/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.List;

/**
 * 文本分片配置。包括分段方式设置、层级解析模式设置、标题层级深度设置、标题保存方式设置、分段长度配置、标题匹配pattern配置等。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SplitConf {

    private SplitModeEnum splitMode = SplitModeEnum.AUTO;

    @Valid
    @Size(min = 1, max = 100)
    private List<@Length(min = 1, max = 128) String> separatorIds = null;

    @Length(min = 1, max = 256)
    private String ruleRegexId = null;

    @Range(min = 0L, max = 6000L)
    private Integer chunkSize = 500;

    @Range(min = 1L, max = 10L)
    private Integer titleLevel = 3;

    private Boolean combineTitle = null;

    private Boolean mergeTitles = null;

    /**
     * 分段设置/层级解析模型 - LENGTH-长度拆分，即为字数拆分 - CATALOG-层级分段下的自动解析 - RULE- 层级分段下的规则解析 - AUTO- 自动拆分，自动识别文档格式匹配适合的拆分解析方式
     */
    public enum SplitModeEnum {
        LENGTH("LENGTH"),

        CATALOG("CATALOG"),

        RULE("RULE"),

        AUTO("AUTO");

        private String value;

        SplitModeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static SplitModeEnum fromValue(String text) {
            for (SplitModeEnum b : SplitModeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }
}
