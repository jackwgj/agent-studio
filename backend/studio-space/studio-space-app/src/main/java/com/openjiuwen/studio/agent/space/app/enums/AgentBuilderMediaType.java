/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.enums;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;

import java.util.Locale;

/**
 * 任务支持下载的文件类型
 */
@Getter
@Slf4j
public enum AgentBuilderMediaType {
    TXT("txt", MediaType.TEXT_PLAIN),
    PDF("pdf", MediaType.APPLICATION_PDF),
    JS("js", MediaType.parseMediaType("application/javascript")),
    JSX("jsx", MediaType.parseMediaType("application/javascript")),
    PYTHON("py", MediaType.parseMediaType("text/x-python")),
    MARKDOWN("md", MediaType.TEXT_MARKDOWN),
    JPG("jpg", MediaType.IMAGE_JPEG),
    PNG("png", MediaType.IMAGE_PNG),
    ZIP("zip", MediaType.parseMediaType("application/zip")),
    XLSX("xlsx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    DOCX("docx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    PPTX("pptx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
    ;

    private final String value;

    private final MediaType mediaType;

    AgentBuilderMediaType(String value, MediaType mediaType) {
        this.value = value;
        this.mediaType = mediaType;
    }

    // 静态查找方法：根据value查询匹配的枚举常量
    public static AgentBuilderMediaType fromValue(String value) {
        for (AgentBuilderMediaType enumIns : AgentBuilderMediaType.values()) {
            if (enumIns.value.equals(value)) {
                return enumIns;
            }
        }
        log.warn("No matching constant for value: {}", value);
        return null;
    }

    @NotNull
    public static MediaType matchFileType(String fileName) {
        MediaType mediaType;
        String suffix = StringUtils.substringAfterLast(fileName, '.').toLowerCase(Locale.ROOT);
        log.info("file {} suffix is: {}", fileName, suffix);
        AgentBuilderMediaType fileType = fromValue(suffix);
        if (ObjectUtils.isNotEmpty(fileType)) {
            mediaType = fileType.getMediaType();
        } else {
            mediaType = MediaType.TEXT_PLAIN;
        }
        return mediaType;
    }
}
