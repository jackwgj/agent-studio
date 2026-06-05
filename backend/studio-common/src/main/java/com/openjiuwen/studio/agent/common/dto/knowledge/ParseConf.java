/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件解析配置-参数配置，包含是否使用OCR增强、是否解析图片、解析图片是否需要提取文字、是否解析页眉页脚、是否解析目录页等
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ParseConf {

    private Boolean ocrEnabled = false;

    private Boolean imageEnabled = false;

    private Boolean headerFooterEnabled = false;

    private Boolean catalogEnabled = null;

    private ImageConfEnum imageConf = null;

    /**
     * 图片解析开启后（TEXT 提取图片文本、IMAGE 保留原图）
     */
    public enum ImageConfEnum {
        TEXT("TEXT"),

        IMAGE("IMAGE");

        private String value;

        ImageConfEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ImageConfEnum fromValue(String text) {
            for (ImageConfEnum b : ImageConfEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
